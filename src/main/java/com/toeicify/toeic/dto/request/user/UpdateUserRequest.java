package com.toeicify.toeic.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

/**
 * Created by hungpham on 7/9/2025
 */
public record UpdateUserRequest(
        @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Full name must not contain numbers or special characters")
        @NotBlank(message = "Full name is required")
        String fullName,
        @Pattern(regexp = "^\\S+$", message = "Username must not contain spaces")
        @NotBlank(message = "Username is required")
        String username,
        @Email(message = "Email is not valid")
        @NotBlank(message = "Email is required")
        String email,
        Instant examDate,
        Integer targetScore  ) {
}
