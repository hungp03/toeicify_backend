package com.toeicify.toeic.dto.request.schedule;

/**
 * Created by hungpham on 8/31/2025
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateStudyScheduleRequest(
        @NotBlank String title,
        String description,
        List<CreateTodoRequest> todos
) {}
