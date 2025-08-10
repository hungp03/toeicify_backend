package com.toeicify.toeic.dto.response.exampart;

public record MissingPartResponse(
        Integer partNumber,
        String partName,
        Integer expectedQuestionCount
) {}
