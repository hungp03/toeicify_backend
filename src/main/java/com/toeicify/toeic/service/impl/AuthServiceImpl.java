package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.config.CustomUserDetails;
import com.toeicify.toeic.dto.request.AuthRequest;
import com.toeicify.toeic.dto.response.auth.AuthResponse;
import com.toeicify.toeic.dto.response.user.UserInfoResponse;
import com.toeicify.toeic.dto.response.user.UserLoginResponse;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.service.AuthService;
import com.toeicify.toeic.service.JwtService;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public AuthResponse login(AuthRequest request) {
        final String identifier = request.identifier();
        User currentUser = userService.findByUsernameOrEmail(identifier);
        checkPasswordExists(currentUser);
        Authentication authentication = authenticate(request.identifier(), request.password());
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        UserLoginResponse userLoginResponse = UserLoginResponse.from(currentUser, accessToken);
        return AuthResponse.of(userLoginResponse, refreshToken);
    }

    @Override
    public AuthResponse renewToken(String refreshToken) {
        if (refreshToken == null || refreshToken.equals("none")) {
            throw new ResourceNotFoundException("Please sign in first");
        }

        Jwt decodedRefreshToken = jwtService.decode(refreshToken);
        String jti = decodedRefreshToken.getId();
        String username = decodedRefreshToken.getSubject();

        String blacklistKey = "rt_revoked:" + jti;
        Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
        if (isBlacklisted) {
            throw new ResourceInvalidException("Refresh token is revoked");
        }
        // Check expired
        Instant now = Instant.now();
        Instant exp = decodedRefreshToken.getExpiresAt();
        if (exp != null && now.isAfter(exp)) {
            throw new ResourceInvalidException("Refresh token is expired");
        }
        User user = userService.findByUsernameOrEmail(username);
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        // Thêm jti cũ vào blacklist
        Duration ttl = Duration.between(now, exp);
        redisTemplate.opsForValue().set(blacklistKey, "1", ttl);
        UserLoginResponse userLoginResponse = UserLoginResponse.from(user, newAccessToken);
        return AuthResponse.of(userLoginResponse, newRefreshToken);
    }


    @Override
    public UserInfoResponse getUserInfo() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userService.findByUsernameOrEmail(username);
        return UserInfoResponse.from(user);
    }

    @Override
    public void logout(String authHeader, String refreshToken) {
        if (authHeader == null || !authHeader.startsWith("Bearer ") ||
                refreshToken == null || refreshToken.equals("none")) {
            throw new ResourceInvalidException("Please sign in first");
        }
        String accessToken = authHeader.substring(7);
        blacklistToken(accessToken, "at_revoked");
        blacklistToken(refreshToken, "rt_revoked");
    }

    private void blacklistToken(String token, String key) {
        try {
            Jwt decodedToken = jwtService.decode(token);
            String refreshJti = decodedToken.getId();
            Instant refreshExp = decodedToken.getExpiresAt();
            if (refreshJti != null && refreshExp != null && refreshExp.isAfter(Instant.now())) {
                Duration ttl = Duration.between(Instant.now(), refreshExp);
                redisTemplate.opsForValue().set(key + ":" + refreshJti, "1", ttl);
            }
        } catch (Exception ignored) {
        }
    }


    private void checkPasswordExists(User user) {
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new BadCredentialsException("Your account has no password.");
        }
    }

    private Authentication authenticate(String username, String password) {
        return authenticationManagerBuilder.getObject().authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }
}
