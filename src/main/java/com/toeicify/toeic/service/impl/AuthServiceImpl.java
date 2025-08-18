package com.toeicify.toeic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.config.CustomUserDetails;
import com.toeicify.toeic.dto.request.auth.AuthRequest;
import com.toeicify.toeic.dto.request.auth.RegisterRequest;
import com.toeicify.toeic.dto.request.auth.ResetPasswordRequest;
import com.toeicify.toeic.dto.response.auth.AuthResponse;
import com.toeicify.toeic.dto.response.auth.OtpVerificationResponse;
import com.toeicify.toeic.dto.response.user.UserInfoResponse;
import com.toeicify.toeic.dto.response.user.UserLoginResponse;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.AccessDeniedException;
import com.toeicify.toeic.exception.ResourceAlreadyExistsException;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.mapper.UserMapper;
import com.toeicify.toeic.service.*;
import com.toeicify.toeic.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PasswordEncoder passwordEncoder;
    private final IdentifyCodeService identifyCodeService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    public AuthResponse login(AuthRequest request) {
        final String identifier = request.identifier();
        User currentUser = userService.findByUsernameOrEmail(identifier);
        checkAccountActive(currentUser);
        checkPasswordExists(currentUser);
        Authentication authentication = authenticate(request.identifier(), request.password());
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        UserLoginResponse userLoginResponse = UserLoginResponse.from(currentUser, accessToken);
        notificationService.saveNotification(currentUser.getUserId(), "Thông báo", "Bạn đang đăng nhập ở nơi khác, nếu không phải bạn, vui lòng thực hiện các thao tác bảo mật");
        return AuthResponse.of(userLoginResponse, refreshToken);
    }

    private void checkAccountActive(User currentUser) {
        if (!currentUser.getIsActive()) {
            throw new AccessDeniedException("User is locked");
        }
    }

    @Override
    public AuthResponse renewToken(String refreshToken) {
        if (refreshToken == null || refreshToken.equals("none")) {
            throw new ResourceNotFoundException("Please sign in first");
        }

        Jwt decodedRefreshToken = jwtService.decode(refreshToken);
        String jti = decodedRefreshToken.getId();
        Long uid = Long.valueOf(decodedRefreshToken.getSubject());

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
        User user = userService.findById(uid);
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
        Long uid = SecurityUtil.getCurrentUserId();
        User user = userService.findById(uid);
        return userMapper.toUserInfoResponse(user);
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

    @Override
    public void registerUser(RegisterRequest request) {
        if (!Objects.equals(request.password(), request.confirmPassword())) {
            throw new ResourceInvalidException("Passwords do not match.");
        }

        if (userService.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("User with this email already exists.");
        }

        if (userService.existsByUsername(request.username())) {
            throw new ResourceAlreadyExistsException("User with this username already exists.");
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        RegisterRequest requestWithEncodedPassword = new RegisterRequest(
                request.fullName(),
                request.username(),
                request.email(),
                encodedPassword,
                encodedPassword
        );
        String token = jwtService.generateRegisterToken(request.username(), request.email());
        try {
            String keyPrefix = "REG:";
            String redisKey = keyPrefix + request.email();
            String userJson = objectMapper.writeValueAsString(requestWithEncodedPassword);
            redisTemplate.opsForValue().set(redisKey, userJson, 10, TimeUnit.MINUTES);
            emailService.sendRegisterVerificationEmail(request.email(), token, "register");
        } catch (Exception e) {
            throw new RuntimeException("Error during registration", e);
        }
    }

    @Override
    public boolean verifyRegisterToken(String token) throws JsonProcessingException {
        Map<String, String> userInfo = jwtService.verifyRegisterToken(token);
        String email = userInfo.get("email");
        String username = userInfo.get("username");
        String keyPrefix = "REG:";
        String redisKey = keyPrefix + email;
        String userJson = redisTemplate.opsForValue().get(redisKey);
        if (userJson != null) {
            RegisterRequest redisUser = objectMapper.readValue(userJson, RegisterRequest.class);
            if (redisUser.username().equals(username)) {
                userService.register(redisUser);
                redisTemplate.delete(email);
                return true;
            }
        }
        return false;
    }

    @Override
    public void forgotPassword(String email) {
        if (!userService.existsByEmail(email)) {
            throw new ResourceNotFoundException("Email " + email + " not found");
        }
        String otp = identifyCodeService.generateOTP(email);
        emailService.sendForgotPasswordEmail(email, otp, "forgotPassword");
    }

    @Override
    @Transactional
    public OtpVerificationResponse verifyOtp(String email, String inputOtp) {
        boolean validOTP = identifyCodeService.validateCode("OTP:" + email, inputOtp);
        if (!validOTP) {
            throw new ResourceInvalidException("OTP is not valid or expired");
        }
        identifyCodeService.deleteCode("OTP:" + email);
        String identifyCode = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("RESET:" + email, identifyCode, 3, TimeUnit.MINUTES);
        return new OtpVerificationResponse(identifyCode);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        boolean isValidCode = identifyCodeService.validateCode("RESET:" + request.email(), request.identifyCode());
        if (!isValidCode) {
            throw new ResourceInvalidException("Identify code is not valid");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResourceInvalidException("Confirm passwords do not match.");
        }
        identifyCodeService.deleteCode("RESET:" + request.email());
        userService.resetPassword(request.email(), request.newPassword());
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
