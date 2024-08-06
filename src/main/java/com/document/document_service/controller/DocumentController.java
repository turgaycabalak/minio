package com.document.document_service.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.util.List;
import java.util.Objects;

import com.document.document_service.mapper.BucketMapper;
import com.document.document_service.service.DocumentService;

import io.minio.messages.Bucket;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/document")
public class DocumentController {
  private final DocumentService documentService;

  @GetMapping("/bucket")
  public List<BucketResponse> getAllBuckets() {
    List<Bucket> buckets = documentService.getAllBuckets();
    return BucketMapper.toDtoList(buckets);
  }

  @GetMapping("/bucket/{bucketName}")
  public BucketResponse saveBucket(@PathVariable("bucketName") String bucketName) throws Exception {
    Bucket bucket = documentService.saveBucket(bucketName);
    return BucketMapper.toDto(bucket);
  }

  @PostMapping(value = "/{bucketName}", consumes = MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Resource> uploadFile(@PathVariable("bucketName") String bucketName,
                                             @RequestPart("file") MultipartFile file) throws Exception {
    MultipartFile uploadedFile = documentService.uploadFile(bucketName, file);
    return ResponseEntity.ok()
        .contentType(MediaType.valueOf(Objects.requireNonNull(uploadedFile.getContentType())))
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + uploadedFile.getOriginalFilename() + "\"")
        .body(new ByteArrayResource(uploadedFile.getBytes()));
  }

}
