package com.capstone.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiServerProperties.class)
public class AiServerConfig {

    @Bean
    public RestClient aiServerRestClient(AiServerProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.serverBaseUrl())
                .defaultHeader("ngrok-skip-browser-warning", "true")
                .build();
    }
}
