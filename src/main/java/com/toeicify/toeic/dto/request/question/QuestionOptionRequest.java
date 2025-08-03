package com.toeicify.toeic.dto.request.question;

/**
 * Created by hungpham on 8/3/2025
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QuestionOptionRequest(
        Long optionId, // null khi tạo mới

        @NotBlank(message = "Option letter is required")
        @Pattern(regexp = "[ABCD]", message = "Option letter must be A, B, C, or D")
        String optionLetter, // A, B, C for Part 2,3,4 | A, B, C, D for Part 1,5,6,7

        @NotBlank(message = "Option text is required")
        String optionText
) {}
