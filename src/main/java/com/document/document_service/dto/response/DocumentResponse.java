package com.document.document_service.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record DocumentResponse(
    String id,
    String name,
    Long size,
    String extension,
    LocalDateTime createdDate,
    String description
) {
}
