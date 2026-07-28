package com.capstone.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(
        String region,
        String accessKey,
        String secretKey,
        String bucket,
        String publicBaseUrl,
        String keyPrefix,
        long maxFileSizeBytes
) {
}
