package com.toeicify.toeic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.toeicify.toeic.dto.request.auth.AuthRequest;
import com.toeicify.toeic.dto.request.auth.RegisterRequest;
import com.toeicify.toeic.dto.request.auth.ResetPasswordRequest;
import com.toeicify.toeic.dto.response.auth.AuthResponse;
import com.toeicify.toeic.dto.response.auth.OtpVerificationResponse;
import com.toeicify.toeic.dto.response.user.UserInfoResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public interface AuthService {

    AuthResponse login(AuthRequest request);
    AuthResponse renewToken(String refreshToken);
    UserInfoResponse getUserInfo();
    void logout(String accessToken, String refreshToken);
    void registerUser(RegisterRequest request);
    boolean verifyRegisterToken(String token) throws JsonProcessingException;
    void forgotPassword(String email);
    OtpVerificationResponse verifyOtp(String email, String inputOtp);
    void resetPassword(ResetPasswordRequest request);
}
