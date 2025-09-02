package com.toeicify.toeic.dto.response.schedule;

/**
 * Created by hungpham on 8/31/2025
 */
import java.time.LocalDateTime;

public record ScheduleTodoResponse(
        Long todoId,
        String taskDescription,
        Boolean isCompleted,
        LocalDateTime dueDate
) {}
