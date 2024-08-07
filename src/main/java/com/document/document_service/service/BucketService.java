package com.document.document_service.service;

import java.util.List;
import java.util.stream.StreamSupport;

import com.document.document_service.dto.response.BucketResponse;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.SetBucketVersioningArgs;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.VersioningConfiguration;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BucketService {
  private final MinioClient minioClient;

  public List<Bucket> getAllBuckets() {
    try {
      return minioClient.listBuckets();
    } catch (Exception e) {
      throw new IllegalStateException("Minio client error!");
    }
  }

  public List<BucketResponse> getAllBucketDetails() throws Exception {
    List<Bucket> buckets = minioClient.listBuckets();

    return buckets.stream()
        .map(bucket -> {
          Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
              .bucket(bucket.name())
              .includeVersions(true)
              .build());
          List<Item> itemList = StreamSupport.stream(results.spliterator(), false)
              .map(itemResult -> {
                try {
                  return itemResult.get();
                } catch (Exception e) {
                  throw new RuntimeException("Error processing item", e);
                }
              })
              .toList();

          long totalSize = itemList.stream()
              .map(Item::size)
              .reduce(0L, Long::sum);
          double totalSizeInKib = totalSize / 1024.0; // byte to kib

          return BucketResponse.builder()
              .name(bucket.name())
              .creationDate(bucket.creationDate().toLocalDateTime())
              .fileCount((long) itemList.size())
              .totalSize(totalSizeInKib)
              .build();
        })
        .toList();
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

    VersioningConfiguration versioningConfig =
        new VersioningConfiguration(VersioningConfiguration.Status.ENABLED, false);
    SetBucketVersioningArgs versioningArgs = SetBucketVersioningArgs.builder()
        .bucket(bucketName)
        .config(versioningConfig)
        .build();
    minioClient.setBucketVersioning(versioningArgs);

    return minioClient.listBuckets().stream()
        .filter(bucket -> bucket.name().equals(bucketName))
        .findFirst()
        .orElse(null);
  }

  public void deleteBucket(String bucketName) throws Exception {
    // Fetch all file objects from minio (to handle 'objectName' and 'versionId' to delete)
    Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
        .bucket(bucketName)
        .includeVersions(true)
        .build());

    // convert into Item object of files in related bucket
    List<Item> list = StreamSupport.stream(results.spliterator(), false)
        .map(itemResult -> {
          try {
            return itemResult.get();
          } catch (Exception e) {
            throw new RuntimeException("Error processing item", e);
          }
        })
        .toList();

    // remove all files by using 'objectName' and 'versionId'
    list.forEach(item -> {
      try {
        minioClient.removeObject(RemoveObjectArgs.builder()
            .bucket(bucketName)
            .object(item.objectName())
            .versionId(item.versionId())
            .build());
      } catch (Exception e) {
        throw new RuntimeException("Error processing item", e);
      }
    });

    // remove bucket
    minioClient.removeBucket(RemoveBucketArgs.builder()
        .bucket(bucketName)
        .build());
  }
}
