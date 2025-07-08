package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.AuthRequest;
import com.toeicify.toeic.dto.response.auth.AuthResponse;
import com.toeicify.toeic.dto.response.user.UserInfoResponse;
import com.toeicify.toeic.dto.response.user.UserLoginResponse;
import com.toeicify.toeic.service.AuthService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @PostMapping("login")
    @ApiMessage("Login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody AuthRequest authRequest) {
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

}

