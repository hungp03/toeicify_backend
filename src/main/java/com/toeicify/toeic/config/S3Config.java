package com.toeicify.toeic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Created by hungpham on 7/11/2025
 */
@Configuration
public class S3Config {
    @Value("${cloud.endpoint}")
    private String endpoint;

    @Value("${cloud.accessKey}")
    private String accessKey;

    @Value("${cloud.secretKey}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        S3Configuration serviceConfig = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build();

        return S3Client.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.AP_SOUTHEAST_1)
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(serviceConfig)
                .build();
    }
}

