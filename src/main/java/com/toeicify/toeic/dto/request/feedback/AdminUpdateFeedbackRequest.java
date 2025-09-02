package com.toeicify.toeic.dto.request.feedback;

import com.toeicify.toeic.util.enums.FeedbackStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Created by hungpham on 8/19/2025
 */
public record AdminUpdateFeedbackRequest(
        @NotNull
        FeedbackStatus status,
        @NotBlank
        @Pattern(
                regexp = "^[\\p{L}\\p{N}\\s.,!?\"'():;\\-…“”‘’\\n\\r\\t]+$",
                flags = { Pattern.Flag.UNICODE_CASE },
                message = "Content must contain only letters, numbers, spaces and punctuation"
        )
        String adminNote
) {
}
