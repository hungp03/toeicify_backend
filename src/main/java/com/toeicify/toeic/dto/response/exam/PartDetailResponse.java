package com.toeicify.toeic.dto.response.exam;

import lombok.Builder;

/**
 * Created by hungpham on 8/9/2025
 */
@Builder
public record PartDetailResponse(
        Integer partNumber,
        String partName,
        Integer correctAnswers,
        Integer totalQuestions,
        Double accuracyPercent
) {}

