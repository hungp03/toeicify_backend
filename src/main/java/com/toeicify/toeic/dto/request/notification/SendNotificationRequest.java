package com.toeicify.toeic.dto.request.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Created by hungpham on 8/18/2025
 */
public record SendNotificationRequest(
        @NotNull
        Long userId,
        @NotBlank
        String title,
        @NotBlank
        String content
) {
}
