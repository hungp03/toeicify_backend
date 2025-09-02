package com.toeicify.toeic.dto.request.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Created by hungpham on 9/2/2025
 */
public record CreateSingleTodoRequest (
    @NotNull(message = "scheduleId is required")
    Long scheduleId,

    @NotBlank(message = "Job description cannot be blank")
    @Size(max = 200, message = "Description maximum 200 characters")
    @Pattern(
            regexp = "^[\\p{L}\\p{M}\\p{N}\\p{Zs}0-9.,'’()\\-:;!?]*$",
            message = "The description must contain only letters, numbers, spaces and some basic punctuation"
    )
    String taskDescription,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss][.SSS]")
    @FutureOrPresent(message = "dueDate cannot be in the past")
    LocalDateTime dueDate
){}
