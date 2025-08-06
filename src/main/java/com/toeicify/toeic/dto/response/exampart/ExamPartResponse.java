package com.toeicify.toeic.dto.response.exampart;

import com.toeicify.toeic.dto.response.question.QuestionGroupResponse;

import java.util.List;

/**
 * Created by hungpham on 7/10/2025
 */
public record ExamPartResponse(
        Long partId,
        Integer partNumber,
        String partName,
        String description,
        Integer questionCount,
        List<QuestionGroupResponse> questionGroups
) {}
