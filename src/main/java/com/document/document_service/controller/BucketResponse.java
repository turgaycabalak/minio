package com.document.document_service.controller;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record BucketResponse(
    String name,
    LocalDateTime creationDate
) {
}
