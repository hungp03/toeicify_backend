package com.toeicify.toeic.dto.request.question;

/**
 * Created by hungpham on 8/3/2025
 */
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuestionGroupRequest(
        Long groupId, // null khi tạo mới

        @NotNull(message = "Part ID is required")
        Long partId,

        String passageText, // Part 6, 7, (1,2,3,4 -> passageText đóng vai trò như transcript)
        String imageUrl,    // Part 1, 3, 4, 6, 7
        String audioUrl,    // Part 1, 2, 3, 4

        @NotEmpty(message = "Group must have questions")
        @Valid
        List<QuestionRequest> questions
) {}