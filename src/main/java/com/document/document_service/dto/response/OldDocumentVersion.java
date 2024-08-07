package com.document.document_service.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record OldDocumentVersion(
    Long size,
    LocalDateTime createdDate,
    String versionId,
    String version
) {
}
