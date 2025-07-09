package com.toeicify.toeic.dto.request.user;

import com.toeicify.toeic.util.constant.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Created by hungpham on 7/9/2025
 */
public record UpdatePasswordRequest(
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ValidationPatterns.PASSWORD_PATTERN, message = "Password cannot contain spaces")
        @NotBlank String currentPassword,
        @NotNull(message = "Password cannot be null")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ValidationPatterns.PASSWORD_PATTERN, message = "Password cannot contain spaces")
        String newPassword,

        @NotNull(message = "Confirm Password cannot be null")
        String confirmPassword
) {
}
