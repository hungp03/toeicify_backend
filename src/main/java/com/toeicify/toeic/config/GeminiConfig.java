package com.toeicify.toeic.config;

/**
 * Created by hungpham on 8/21/2025
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfig {
    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
