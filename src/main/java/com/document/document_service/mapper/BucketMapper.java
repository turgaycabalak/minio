package com.document.document_service.mapper;

import java.util.List;
import java.util.Objects;

import com.document.document_service.controller.BucketResponse;

import io.minio.messages.Bucket;

public final class BucketMapper {
  private BucketMapper() {
  }

  public static BucketResponse toDto(Bucket bucket) {
    if (Objects.isNull(bucket)) {
      return null;
    }

    return BucketResponse.builder()
        .name(bucket.name())
        .creationDate(bucket.creationDate().toLocalDateTime())
        .build();
  }

  public static List<BucketResponse> toDtoList(List<Bucket> buckets) {
    if (Objects.isNull(buckets) || buckets.isEmpty()) {
      return List.of();
    }

    return buckets.stream()
        .map(BucketMapper::toDto)
        .toList();
  }
}
