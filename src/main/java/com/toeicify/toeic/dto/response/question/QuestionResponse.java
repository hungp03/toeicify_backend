package com.toeicify.toeic.dto.response.question;

/**
 * Created by hungpham on 8/3/2025
 */
import com.toeicify.toeic.util.enums.QuestionType;

import java.util.List;

public record QuestionResponse(
        Long questionId,
        Long groupId,
        String questionText,
        QuestionType questionType,
        String correctAnswerOption,
        String explanation,
        List<QuestionOptionResponse> options
) {}
