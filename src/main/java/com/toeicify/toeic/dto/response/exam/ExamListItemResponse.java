package com.toeicify.toeic.dto.response.exam;

/**
 * Created by hungpham on 7/20/2025
 */
public record ExamListItemResponse(
        Long examId,
        String examName,
        String examDescription,
        Integer totalQuestions,
        String categoryName,
        Integer totalParts,
        String status
) {}

