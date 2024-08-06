package com.document.document_service.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import io.minio.MinioClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
  @Value("${minio.url}")
  private String minioUrl;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Bean
  public MinioClient minioClient() {
    MinioClient build = MinioClient.builder()
        .endpoint(minioUrl)
        .credentials(accessKey, secretKey)
        .build();

    try {
      build.ignoreCertCheck();
    } catch (KeyManagementException | NoSuchAlgorithmException e) {
      throw new RuntimeException("Error creating MinioClient", e);
    }

    return build;
  }
}
