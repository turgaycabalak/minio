package com.document.document_service.dto.request;

public record DownloadFileRequest(
    String bucketName,
    String fileName,
    String versionId
) {
}
