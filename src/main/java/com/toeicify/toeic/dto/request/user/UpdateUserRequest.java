package com.toeicify.toeic.dto.request.user;

import com.toeicify.toeic.util.constant.ValidationPatterns;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Created by hungpham on 7/9/2025
 */
public record UpdateUserRequest(
        @Pattern(regexp = ValidationPatterns.FULL_NAME_PATTERN, message = "Full name must not contain numbers or special characters")
        @NotBlank(message = "Full name is required")
        String fullName,
        @NotNull(message = "Username cannot be null")
        @Pattern(regexp = ValidationPatterns.USERNAME_PATTERN, message = "Username cannot contain special characters or spaces, only -, _, ., and + are allowed")
        String username,
        @NotNull(message = "Email cannot be null")
        @Email(message = "Invalid email format")
        @Pattern(regexp = ValidationPatterns.EMAIL_PATTERN, message = "Email cannot contain special characters or spaces, only -, _, ., and + are allowed")
        String email,
        @FutureOrPresent(message = "Exam date must be today or in the future")
        LocalDate examDate,
        @Min(value = 0, message = "Target score must be between 0 and 990")
        @Max(value = 990, message = "Target score must be between 0 and 990")
        Integer targetScore ) {
        public boolean isTargetScoreValid() {
                return targetScore != null && targetScore % 5 == 0;
        }
}
