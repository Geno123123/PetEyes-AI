package com.capstone.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.naver.search")
public record NaverProperties(
        String clientId,
        String clientSecret
) {
}
