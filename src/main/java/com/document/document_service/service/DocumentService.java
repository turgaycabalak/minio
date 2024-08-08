package com.document.document_service.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.document.document_service.dto.response.DocumentResponse;
import com.document.document_service.dto.response.OldDocumentVersion;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {
  private final MinioClient minioClient;

  public MultipartFile uploadFile(String bucketName, MultipartFile file) throws Exception {
    minioClient.putObject(PutObjectArgs.builder()
        .bucket(bucketName)
        .object(file.getOriginalFilename())
        .contentType(file.getContentType())
        .stream(file.getInputStream(), file.getSize(), -1)
        .build());

    return file;
  }

  public InputStream downloadFile(String bucketName, String fileName, String versionId) throws Exception {
    GetObjectArgs.Builder getObjectArgsBuilder = GetObjectArgs.builder()
        .bucket(bucketName)
        .object(fileName);

    if (versionId != null && !versionId.isEmpty()) {
      getObjectArgsBuilder.versionId(versionId);
    }

    return minioClient.getObject(getObjectArgsBuilder.build());
  }

  public List<DocumentResponse> getAllDocuments(String bucketName) throws Exception {
    Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
        .bucket(bucketName)
        .includeVersions(true)
        .build());

    Map<String, List<Item>> groupedItems = StreamSupport.stream(results.spliterator(), false)
        .map(itemResult -> {
          try {
            return itemResult.get();
          } catch (Exception e) {
            throw new RuntimeException("Error processing item", e);
          }
        })
        .collect(Collectors.groupingBy(
            Item::objectName,
            Collectors.mapping(
                item -> item,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> list.stream()
                        .sorted(Comparator.comparing(Item::lastModified)) // v1, v2, v3 ..
                        .toList()
                )
            )
        ));

    return groupedItems.values().stream()
        .map(items -> getDocumentResponse(bucketName, items))
        .toList();
  }

  public DocumentResponse getDocument(String bucketName, String fileName) {
    Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
        .bucket(bucketName)
        .includeVersions(true)
        .build());

    List<Item> itemsByFileName = StreamSupport.stream(results.spliterator(), false)
        .map(itemResult -> {
          try {
            return itemResult.get();
          } catch (Exception e) {
            throw new RuntimeException("Error processing item", e);
          }
        })
        .filter(item -> item.objectName().equals(fileName)) // v2, v1, v3, v4
        .sorted(Comparator.comparing(Item::lastModified)) // v1, v2, v3, v4
        .toList();

    return getDocumentResponse(bucketName, itemsByFileName);
  }

  private DocumentResponse getDocumentResponse(String bucketName, List<Item> itemsByFileName) {
    Item itemLastVersioned = itemsByFileName.getLast(); // v4
    List<Item> newToOld = itemsByFileName.stream()
        .filter(item -> item != itemLastVersioned) // v4 removed from list
        .toList();

    List<OldDocumentVersion> oldDocumentVersions = new ArrayList<>();
    for (int i = newToOld.size(); i > 0; i--) {
      Item item = newToOld.get(i - 1);
      oldDocumentVersions.add(OldDocumentVersion.builder()
          .size(item.size())
          .createdDate(LocalDateTime.ofInstant(item.lastModified().toInstant(), ZoneOffset.UTC))
          .versionId(item.versionId())
          .version("v" + (i))
          .build());
    }

    return DocumentResponse.builder()
        .bucketName(bucketName)
        .fileName(itemLastVersioned.objectName())
        .size(itemLastVersioned.size())
        .extension(getFileExtension(itemLastVersioned.objectName()))
        .createdDate(LocalDateTime.ofInstant(itemLastVersioned.lastModified().toInstant(), ZoneOffset.UTC))
        .versionId(itemLastVersioned.versionId())
        .version("v" + (itemsByFileName.size()))
        .oldDocumentVersions(oldDocumentVersions)
        .build();
  }

  private String getFileExtension(String fileName) {
    int lastIndexOfDot = fileName.lastIndexOf(".");
    if (lastIndexOfDot == -1) {
      return "";
    }
    return fileName.substring(lastIndexOfDot + 1);
  }

  public DocumentResponse moveFile(String fileName, String fromBucket, String toBucket) throws Exception {
    Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
        .bucket(fromBucket)
        .includeVersions(true)
        .build());

    StreamSupport.stream(results.spliterator(), false)
        .map(itemResult -> {
          try {
            return itemResult.get();
          } catch (Exception e) {
            throw new RuntimeException("Error processing item", e);
          }
        })
        .filter(item -> item.objectName().equals(fileName))
        .sorted(Comparator.comparing(Item::lastModified)) // v1, v2, v3
        .forEach(item -> {
          try {
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .source(CopySource.builder()
                        .bucket(fromBucket)
                        .object(fileName)
                        .versionId(item.versionId())
                        .build())
                    .bucket(toBucket)
                    .object(fileName)
                    .build());

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(fromBucket)
                    .object(fileName)
                    .versionId(item.versionId())
                    .build()
            );
          } catch (Exception e) {
            throw new RuntimeException("Error during moving file", e);
          }
        });

    return getDocument(toBucket, fileName);
  }

  public void deleteFile(String bucketName, String fileName) {
    Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
        .bucket(bucketName)
        .includeVersions(true)
        .build());

    StreamSupport.stream(results.spliterator(), false)
        .map(itemResult -> {
          try {
            return itemResult.get();
          } catch (Exception e) {
            throw new RuntimeException("Error processing item", e);
          }
        })
        .filter(item -> item.objectName().equals(fileName))
        .forEach(item -> {
          try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .versionId(item.versionId())
                    .build()
            );
          } catch (Exception e) {
            throw new RuntimeException("Error during moving file", e);
          }
        });
  }

  public void deleteAllFiles(String bucketName) {
    Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
        .bucket(bucketName)
        .includeVersions(true)
        .build());

    StreamSupport.stream(results.spliterator(), false)
        .map(itemResult -> {
          try {
            return itemResult.get();
          } catch (Exception e) {
            throw new RuntimeException("Error processing item", e);
          }
        })
        .forEach(item -> {
          try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(item.objectName())
                    .versionId(item.versionId())
                    .build()
            );
          } catch (Exception e) {
            throw new RuntimeException("Error during moving file", e);
          }
        });
  }
}
