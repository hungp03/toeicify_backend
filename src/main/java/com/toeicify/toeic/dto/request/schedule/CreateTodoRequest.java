package com.toeicify.toeic.dto.request.schedule;

/**
 * Created by hungpham on 8/31/2025
 */
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record CreateTodoRequest(
        @NotBlank String taskDescription,
        LocalDateTime dueDate
) {}
