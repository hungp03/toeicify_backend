package com.toeicify.toeic.dto.request.gemini;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Created by hungpham on 8/21/2025
 */
public record ChatRequest(
        @NotBlank(message = "sessionId must not be blank")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "sessionId is allowed to contain only letters, numbers, '-', '_'")
        @Size(max = 64, message = "sessionId maximum 64 characters")
        String sessionId,

        @NotBlank(message = "prompt must not be blank")
        @Size(max = 2000, message = "prompt too long (max 2000 characters)")
        String prompt
) {}
