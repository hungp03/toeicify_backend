package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.config.CustomUserDetails;
import com.toeicify.toeic.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 7/7/2025
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Override
    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    @Override
    public String generateAccessToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS)) // 1 hour
                .subject(String.valueOf(userDetails.getUser().getUserId()))
                .id(UUID.randomUUID().toString()) // jti
                .claim("userId", userDetails.getUser().getUserId())
                .claim("username", userDetails.getUsername())
                .claim("authorities", List.of("ROLE_" + userDetails.getUser().getRole().getRoleId()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public String generateRefreshToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(30, ChronoUnit.DAYS))
                .subject(String.valueOf(userDetails.getUser().getUserId()))
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
