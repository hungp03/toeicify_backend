package com.toeicify.toeic.dto.request.question;

/**
 * Created by hungpham on 8/3/2025
 */
import com.toeicify.toeic.util.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record QuestionRequest(
        Long questionId, // null khi tạo mới
        @NotNull
        @Min(1)
        Integer questionNumber,
        String questionText, // maybe null

        @NotNull(message = "Question type is required")
        QuestionType questionType,

        @NotBlank(message = "Correct answer option is required")
        @Pattern(regexp = "[ABCD]", message = "Correct answer must be A, B, C, or D")
        String correctAnswerOption, // A, B, C for Part 2,3,4 | A, B, C, D for Part 1,5,6,7

        String explanation,

        @NotEmpty(message = "Question must have options")
        @Valid
        List<QuestionOptionRequest> options
) {}
