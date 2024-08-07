package com.document.document_service.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record BucketResponse(
    String name,
    LocalDateTime creationDate
) {
}
