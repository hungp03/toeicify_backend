package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.AuthRequest;
import com.toeicify.toeic.dto.response.auth.AuthResponse;
import com.toeicify.toeic.dto.response.user.UserInfoResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);
    AuthResponse renewToken(String refreshToken);
    UserInfoResponse getUserInfo();
    void logout(String accessToken, String refreshToken);
}
