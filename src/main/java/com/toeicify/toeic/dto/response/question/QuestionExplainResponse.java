package com.toeicify.toeic.dto.response.question;

import com.toeicify.toeic.util.enums.QuestionType;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Created by hungpham on 8/10/2025
 */
public record QuestionExplainResponse(
        Integer questionNumber,
        String questionText,
        String audioUrl,
        String imageUrl,
        QuestionType questionType,
        String correctAnswerOption,
        String explanation,
        List<QuestionOptionResponse> options
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
