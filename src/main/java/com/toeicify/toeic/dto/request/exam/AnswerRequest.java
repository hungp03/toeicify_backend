package com.toeicify.toeic.dto.request.exam;

/**
 * Created by hungpham on 8/9/2025
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AnswerRequest(
        @NotNull(message = "Question ID is required")
        Long questionId,

        @NotBlank(message = "Selected option is required")
        @Pattern(regexp = "^[A-D]$", message = "Selected option must be A, B, C, or D")
        String selectedOption
) {}
