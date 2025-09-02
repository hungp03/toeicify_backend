package com.toeicify.toeic.dto.request.schedule;

/**
 * Created by hungpham on 8/31/2025
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateStudyScheduleRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 255, message = "Title must be less than 255 characters")
        @Pattern(
                regexp = "^[\\p{L}\\p{N}\\s.,!?\"'():;\\-…“”‘’\\n\\r\\t]+$",
                flags = { Pattern.Flag.UNICODE_CASE },
                message = "Content must contain only letters, numbers, spaces and punctuation"
        )
        String title,
        @Size(max = 255, message = "Description must be less than 255 characters")
        String description,
        List<CreateTodoRequest> todos
) {}
