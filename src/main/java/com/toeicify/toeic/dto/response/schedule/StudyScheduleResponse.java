package com.toeicify.toeic.dto.response.schedule;

/**
 * Created by hungpham on 8/31/2025
 */
import java.time.LocalDateTime;
import java.util.List;

public record StudyScheduleResponse(
        Long scheduleId,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long userId,
        List<TodoResponse> todos
) {}

