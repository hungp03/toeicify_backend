package com.toeicify.toeic.dto.response.exam;
import lombok.Builder;
/**
 * Created by hungpham on 8/9/2025
 */
@Builder
public record AnswerDetailResponse(
        Long questionId,
        Integer questionNumber,
        String userAnswer,
        String correctAnswer,
        Boolean isCorrect,
        Integer partNumber
) {}

