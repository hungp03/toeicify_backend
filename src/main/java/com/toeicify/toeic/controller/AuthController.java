package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.auth.*;
import com.toeicify.toeic.dto.response.auth.AuthResponse;
import com.toeicify.toeic.dto.response.auth.OtpVerificationResponse;
import com.toeicify.toeic.dto.response.user.UserInfoResponse;
import com.toeicify.toeic.dto.response.user.UserLoginResponse;
import com.toeicify.toeic.service.AuthService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Value("${app.client}")
    private String client;
    private final AuthService authService;

    @PostMapping("login")
    @ApiMessage("Login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", authResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60L * 60 * 24 * 30)          // 30 days
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(authResponse.user());
    }

    @PostMapping("refresh")
    @ApiMessage("Refresh token")
    public ResponseEntity<UserLoginResponse> refresh(@CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken) {
        AuthResponse authResponse = authService.renewToken(refreshToken);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", authResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60L * 60 * 24 * 30)          // 30 days
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(authResponse.user());
    }

    @GetMapping("me")
    @ApiMessage("Get current user")
    public ResponseEntity<UserInfoResponse> me() {
        return ResponseEntity.ok(authService.getUserInfo());
    }

    @PostMapping("logout")
    @ApiMessage("Logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken) {
        authService.logout(authHeader, refreshToken);
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @PostMapping("register")
    @ApiMessage("Send mail register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("register/verify")
    @ApiMessage("Verify register account")
    public ResponseEntity<Void> verifyAccount(@RequestParam String token) {
        try {
            boolean isVerified = authService.verifyRegisterToken(token);
            String redirectUrl = isVerified
                    ? client + "/authentication/success?isRegister=true"
                    : client + "/authentication/error?isRegister=false";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(client + "/authentication/error?isRegister=false"))
                    .build();
        }
    }

    @PostMapping("forgot-password")
    @ApiMessage("Send forgot password email")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        this.authService.forgotPassword(forgotPasswordRequest.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp")
    @ApiMessage("Verify OTP")
    public ResponseEntity<OtpVerificationResponse> verifyOtp(@Valid @RequestBody OTPResetRequest request) {
        return ResponseEntity.ok(this.authService.verifyOtp(request.email(), request.otp()));
    }

    @PostMapping("reset-password")
    @ApiMessage("Reset password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        this.authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

}

