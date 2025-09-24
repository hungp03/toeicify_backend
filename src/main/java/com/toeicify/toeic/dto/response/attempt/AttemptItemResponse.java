package com.toeicify.toeic.dto.response.attempt;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Builder
public record AttemptItemResponse(
        Long attemptId,
        Boolean fullTest,
        List<Integer> parts, // [1,2,4,6] if practicing multiple parts
        Integer correct, // sum correct (sum(scorePart))
        Integer total, // sum of questions (sum(expectedQuestionCount))
        Integer toeicScore, // null if not a full test
        Instant startTime,
        Instant endTime,
        Long durationSeconds // convenient time rendering
) {}

