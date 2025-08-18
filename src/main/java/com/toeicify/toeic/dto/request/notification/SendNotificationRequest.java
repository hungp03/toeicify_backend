package com.toeicify.toeic.dto.request.notification;

/**
 * Created by hungpham on 8/18/2025
 */
public record SendNotificationRequest(
        Long userId,
        String title,
        String content
) {
}
