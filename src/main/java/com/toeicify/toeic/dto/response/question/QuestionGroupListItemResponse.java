package com.toeicify.toeic.dto.response.question;

/**
 * Created by hungpham on 8/3/2025
 */
public record QuestionGroupListItemResponse(
        Long groupId,
        Long partId,
        String partName,
        Integer questionCount,
        String passageText,
        String imageUrl,
        String audioUrl
) {}
