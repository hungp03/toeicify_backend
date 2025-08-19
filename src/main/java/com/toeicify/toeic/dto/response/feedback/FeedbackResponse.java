package com.toeicify.toeic.dto.response.feedback;

/**
 * Created by hungpham on 8/19/2025
 */
import com.toeicify.toeic.util.enums.FeedbackStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FeedbackResponse(
        Long id,
        String content,
        FeedbackStatus status,
        String adminNote,
        LocalDateTime submittedAt,
        LocalDateTime processedAt,
        String userName,
        List<String> attachments
) {}
