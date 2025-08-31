package com.toeicify.toeic.dto.request.schedule;

import java.time.LocalDateTime;

/**
 * Created by hungpham on 8/31/2025
 */
public record UpdateTodoRequest(
        Long todoId,                // null = create
        String taskDescription,
        Boolean isCompleted,
        LocalDateTime dueDate
) {}
