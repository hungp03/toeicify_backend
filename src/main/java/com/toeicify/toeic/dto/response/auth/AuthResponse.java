package com.toeicify.toeic.dto.response.auth;

public record AuthResponse(UserLoginResponse user, String refreshToken) {
    public static AuthResponse of(UserLoginResponse user, String refreshToken) {
        return new AuthResponse(user, refreshToken);
    }
}
