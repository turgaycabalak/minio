package com.document.document_service.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.StreamSupport;

import com.document.document_service.dto.response.DocumentResponse;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {
  private final MinioClient minioClient;

  public List<Bucket> getAllBuckets() {
    try {
      return minioClient.listBuckets();
    } catch (Exception e) {
      throw new IllegalStateException("Minio client error!");
    }
  }

  public Bucket saveBucket(String bucketName) throws Exception {
    if (bucketName.length() < 3 || bucketName.length() > 63) {
      throw new IllegalArgumentException("Bucket name must be between 3 and 63 characters long.");
    }
    if (!bucketName.matches("^[a-z0-9][a-z0-9.-]+[a-z0-9]$")) {
      throw new IllegalArgumentException("Bucket name must follow S3 naming rules.");
    }

    BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
    boolean exists = minioClient.bucketExists(bucketExistsArgs);
    if (exists) {
      throw new IllegalStateException("The Bucket already exists by name: " + bucketName);
    }

    minioClient.makeBucket(MakeBucketArgs.builder()
        .bucket(bucketName)
        .build());

    return minioClient.listBuckets().stream()
        .filter(bucket -> bucket.name().equals(bucketName))
        .findFirst()
        .orElse(null);
  }

  public MultipartFile uploadFile(String bucketName, MultipartFile file) throws Exception {
    minioClient.putObject(PutObjectArgs.builder()
        .bucket(bucketName)
        .object(file.getOriginalFilename())
        .contentType(file.getContentType())
        .stream(file.getInputStream(), file.getSize(), -1)
        .build());

    return file;
  }

  public InputStream downloadFile(String bucketName, String fileName) throws Exception {
    return minioClient.getObject(GetObjectArgs.builder()
        .bucket(bucketName)
        .object(fileName)
        .build());
  }

  public List<DocumentResponse> listFiles(String bucketName) throws Exception {
    Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
        .bucket(bucketName)
        .build());

    return StreamSupport.stream(results.spliterator(), false)
        .map(result -> {
          try {
            Item item = result.get();
            return DocumentResponse.builder()
                .bucketName(bucketName)
                .fileName(item.objectName())
                .size(item.size())
                .extension(getFileExtension(item.objectName()))
                .createdDate(LocalDateTime.ofInstant(item.lastModified().toInstant(), ZoneOffset.UTC))
                .build();
          } catch (Exception e) {
            throw new RuntimeException("Error processing item", e);
          }
        })
        .toList();
  }

  private String getFileExtension(String fileName) {
    int lastIndexOfDot = fileName.lastIndexOf(".");
    if (lastIndexOfDot == -1) {
      return "";
    }
    return fileName.substring(lastIndexOfDot + 1);
  }
}
