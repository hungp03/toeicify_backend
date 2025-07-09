package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.auth.RegisterRequest;
import com.toeicify.toeic.dto.request.user.UpdatePasswordRequest;
import com.toeicify.toeic.dto.request.user.UpdateUserRequest;
import com.toeicify.toeic.dto.response.user.UserUpdateResponse;
import com.toeicify.toeic.entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public interface UserService {
    User findByUsernameOrEmail(String identifier);
    User register(RegisterRequest request);
    User findById(Long uid);
    UserUpdateResponse updateCurrentUser(UpdateUserRequest request);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void changePassword(UpdatePasswordRequest request);
    void resetPassword(String email, String newPassword);
}
