package com.document.document_service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record DocumentResponse(
    String bucketName,
    String fileName,
    Long size,
    String extension,
    LocalDateTime createdDate,
    String versionId,
    String version,
    List<OldDocumentVersion> oldDocumentVersions
) {
}
