package com.toeicify.toeic.dto.request.feedback;

import com.toeicify.toeic.util.enums.FeedbackStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Created by hungpham on 8/19/2025
 */
public record AdminUpdateFeedbackRequest(
        @NotNull
        FeedbackStatus status,
        @NotBlank
        String adminNote
) {
}
