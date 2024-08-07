package com.document.document_service.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BucketResponse(
    String name,
    LocalDateTime creationDate,
    Long fileCount,

    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "0.0") Double totalSize
) {
}
