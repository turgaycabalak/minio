package com.document.document_service.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.document.document_service.dto.response.BucketResponse;
import com.document.document_service.dto.response.DocumentResponse;
import com.document.document_service.mapper.BucketMapper;
import com.document.document_service.service.DocumentService;

import io.minio.MinioClient;
import io.minio.messages.Bucket;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/document")
public class DocumentController {
  private final DocumentService documentService;
  private final MinioClient minioClient;

  @GetMapping("/buckets")
  public List<BucketResponse> getAllBuckets() {
    List<Bucket> buckets = documentService.getAllBuckets();
    return BucketMapper.toDtoList(buckets);
  }

  @PostMapping("/bucket/{bucketName}")
  public BucketResponse saveBucket(@PathVariable("bucketName") String bucketName) throws Exception {
    Bucket bucket = documentService.saveBucket(bucketName);
    return BucketMapper.toDto(bucket);
  }

  @PostMapping(value = "/file/{bucketName}", consumes = MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Resource> uploadFile(@PathVariable("bucketName") String bucketName,
                                             @RequestPart("file") MultipartFile file) throws Exception {
    MultipartFile uploadedFile = documentService.uploadFile(bucketName, file);
    String encodedFileName = URLEncoder.encode(Objects.requireNonNull(uploadedFile.getOriginalFilename()),
        StandardCharsets.UTF_8);

    return ResponseEntity.ok()
        .contentType(MediaType.valueOf(Objects.requireNonNull(uploadedFile.getContentType())))
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFileName + "\"")
        .body(new ByteArrayResource(uploadedFile.getBytes()));
  }

  @GetMapping("/file/{bucketName}/{fileName}")
  public ResponseEntity<Resource> downloadFile(@PathVariable("bucketName") String bucketName,
                                               @PathVariable("fileName") String fileName,
                                               @RequestParam(value = "versionId", required = false) String versionId)
      throws Exception {
    InputStream data = documentService.downloadFile(bucketName, fileName, versionId);
    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFileName + "\"")
        .body(new InputStreamResource(data));
  }

  @GetMapping("/bucket/{bucketName}/files")
  public List<DocumentResponse> listFiles(@PathVariable("bucketName") String bucketName) throws Exception {
    return documentService.listFiles(bucketName);
  }
}
