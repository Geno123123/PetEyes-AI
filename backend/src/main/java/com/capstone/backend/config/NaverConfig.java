package com.capstone.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(NaverProperties.class)
public class NaverConfig {

    @Bean
    public RestClient naverOpenApiRestClient() {
        return RestClient.builder()
                .baseUrl("https://openapi.naver.com")
                .build();
    }
}
