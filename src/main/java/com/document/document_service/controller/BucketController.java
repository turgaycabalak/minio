package com.document.document_service.controller;

import java.util.List;

import com.document.document_service.dto.response.BucketResponse;
import com.document.document_service.mapper.BucketMapper;
import com.document.document_service.service.BucketService;

import io.minio.messages.Bucket;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/bucket")
public class BucketController {
  private final BucketService bucketService;

  @GetMapping
  public List<BucketResponse> getAllBuckets() {
    List<Bucket> buckets = bucketService.getAllBuckets();
    return BucketMapper.toDtoList(buckets);
  }

  @GetMapping("/detail")
  public List<BucketResponse> getAllBucketDetails() throws Exception {
    return bucketService.getAllBucketDetails();
  }

  @GetMapping("/detail/{bucketName}")
  public BucketResponse getAllBucketDetailsByName(@PathVariable("bucketName") String bucketName)
      throws Exception {
    return bucketService.getAllBucketDetailsByName(bucketName);
  }

  @PostMapping("/{bucketName}")
  public BucketResponse saveBucket(@PathVariable("bucketName") String bucketName) throws Exception {
    Bucket bucket = bucketService.saveBucket(bucketName);
    return BucketMapper.toDto(bucket);
  }

  @DeleteMapping("/{bucketName}")
  public void deleteBucket(@PathVariable("bucketName") String bucketName) throws Exception {
    bucketService.deleteBucket(bucketName);
  }
}
