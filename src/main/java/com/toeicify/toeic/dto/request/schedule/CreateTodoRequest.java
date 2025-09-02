package com.toeicify.toeic.dto.request.schedule;

/**
 * Created by hungpham on 8/31/2025
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateTodoRequest(
        @NotBlank(message = "taskDescription cannot be blank")
        @Size(max = 200, message = "taskDescription must be less than 200 characters")
        @Pattern(
                regexp = "^[\\p{L}\\p{N}\\s.,!?\"'():;\\-…“”‘’\\n\\r\\t]+$",
                flags = { Pattern.Flag.UNICODE_CASE },
                message = "Content must contain only letters, numbers, spaces and punctuation"
        )
        String taskDescription,
        LocalDateTime dueDate
) {}
