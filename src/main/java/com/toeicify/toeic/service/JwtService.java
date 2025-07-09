package com.toeicify.toeic.service;

import com.toeicify.toeic.config.CustomUserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Created by hungpham on 7/7/2025
 */
public interface JwtService {
    Jwt decode(String token);
    String generateAccessToken(CustomUserDetails userDetails);
    String generateRefreshToken(CustomUserDetails userDetails);
    String generateRegisterToken(String username, String email);
    Map<String, String> verifyRegisterToken(String token);
}

