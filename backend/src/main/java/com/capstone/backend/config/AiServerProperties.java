package com.capstone.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ai")
public record AiServerProperties(String serverBaseUrl) {}
