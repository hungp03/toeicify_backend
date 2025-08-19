package com.toeicify.toeic.dto.response.exam;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Created by hungpham on 8/9/2025
 */
@Builder
public record ExamResultDetailResponse(
        Long attemptId,
        Boolean isFullTest,
        Integer totalScore,
        Integer listeningScore,
        Integer readingScore,
        Double completionTimeMinutes,
        LocalDateTime startTime,
        LocalDateTime submittedAt,
        List<PartDetailResponse> partsDetail,
        List<AnswerDetailResponse> answersDetail,
        ExamSummaryResponse examSummary
) {}

