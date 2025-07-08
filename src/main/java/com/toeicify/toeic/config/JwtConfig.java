package com.toeicify.toeic.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.toeicify.toeic.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    private SecretKey getSecretKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder(TokenBlacklistService tokenBlacklistService) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Timestamp validator
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();

        // Blacklist validator
        OAuth2TokenValidator<Jwt> blacklistValidator = token -> {
            if (tokenBlacklistService.isBlacklisted(token.getTokenValue())) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Token is blacklisted", null)
                );
            }
            return OAuth2TokenValidatorResult.success();
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestampValidator, blacklistValidator
        ));

        return decoder;
    }
}
