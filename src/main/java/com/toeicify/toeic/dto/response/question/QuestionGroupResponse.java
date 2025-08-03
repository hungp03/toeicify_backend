package com.toeicify.toeic.dto.response.question;

/**
 * Created by hungpham on 8/3/2025
 */
import java.util.List;

public record QuestionGroupResponse(
        Long groupId,
        Long partId,
        String partName,
        String passageText,
        String imageUrl,
        String audioUrl,
        List<QuestionResponse> questions
) {}