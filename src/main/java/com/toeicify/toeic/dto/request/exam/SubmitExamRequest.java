package com.toeicify.toeic.dto.request.exam;

/**
 * Created by hungpham on 8/9/2025
 */
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

public record SubmitExamRequest(
        @NotNull(message = "Exam ID is required")
        Long examId,

        @NotNull(message = "Submit type is required")
        @Pattern(regexp = "^(full|partial)$", message = "Submit type must be 'full' or 'partial'")
        String submitType,

        @Valid
        List<Long> partIds, // For partial submission

        @NotNull(message = "Start time is required")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime endTime,

        @NotEmpty(message = "Answers cannot be empty")
        @Valid
        List<AnswerRequest> answers
) {}
