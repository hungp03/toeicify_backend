package com.toeicify.toeic.dto.request.schedule;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Created by hungpham on 9/1/2025
 */
public record QuickUpdateTodoRequest(
        @Size(min = 1, max = 255, message = "taskDescription must be between 1 and 255 characters")
        String taskDescription,

        LocalDateTime dueDate,

        Boolean isCompleted
) {}
