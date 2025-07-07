package com.toeicify.toeic.service;

import com.toeicify.toeic.config.CustomUserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Created by hungpham on 7/7/2025
 */
public interface JwtService {
    String generateAccessToken(CustomUserDetails userDetails);
    String generateRefreshToken(CustomUserDetails userDetails);
}

