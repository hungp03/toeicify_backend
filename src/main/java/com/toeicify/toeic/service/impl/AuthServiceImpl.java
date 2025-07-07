package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.config.CustomUserDetails;
import com.toeicify.toeic.dto.response.user.UserInfoResponse;
import com.toeicify.toeic.entity.RefreshToken;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.dto.request.AuthRequest;
import com.toeicify.toeic.dto.response.auth.AuthResponse;
import com.toeicify.toeic.dto.response.user.UserLoginResponse;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.repository.RefreshTokenRepository;
import com.toeicify.toeic.service.AuthService;
import com.toeicify.toeic.service.JwtService;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public AuthResponse login(AuthRequest request) {
        final String identifier = request.identifier();
        User currentUser = userService.findByUsernameOrEmail(identifier);
        checkPasswordExists(currentUser);
        Authentication authentication = authenticate(request.identifier(), request.password());
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        UserLoginResponse userLoginResponse = UserLoginResponse.from(currentUser, accessToken);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        RefreshToken storedToken = RefreshToken.builder()
                .token(refreshToken)
                .user(currentUser)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .deviceInfo("Demo")
                .revoked(false)
                .build();

        refreshTokenRepository.save(storedToken);

        return AuthResponse.of(userLoginResponse, refreshToken);
    }

    @Override
    public AuthResponse renewToken(String refreshToken) {
        if (refreshToken == null || refreshToken.equals("none")) {
            throw new ResourceNotFoundException("Please sign in first");
        }
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResourceInvalidException("Refresh token invalid"));
        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResourceInvalidException("Refresh token is revoked or expired");
        }

        User currentUser = storedToken.getUser();
        CustomUserDetails userDetails = CustomUserDetails.fromUser(currentUser);
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        storedToken.setToken(newRefreshToken);
        storedToken.setIssuedAt(Instant.now());
        storedToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        refreshTokenRepository.save(storedToken);
        UserLoginResponse userLoginResponse = UserLoginResponse.from(currentUser, newAccessToken);
        return AuthResponse.of(userLoginResponse, newRefreshToken);
    }

    @Override
    public UserInfoResponse getUserInfo() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userService.findByUsernameOrEmail(username);
        return UserInfoResponse.from(user);
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
