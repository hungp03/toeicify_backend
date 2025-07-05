package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.AuthRequest;
import com.toeicify.toeic.dto.response.auth.AuthResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);
}
