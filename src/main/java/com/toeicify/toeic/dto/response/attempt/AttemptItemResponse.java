package com.toeicify.toeic.dto.response.attempt;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Builder
public record AttemptItemResponse(
        Long attemptId,
        Boolean fullTest,
        List<Integer> parts,      // [1,2,4,6] nếu luyện tập nhiều part
        Integer correct,          // tổng đúng (sum(scorePart))
        Integer total,            // tổng câu (sum(expectedQuestionCount))
        Integer toeicScore,       // null nếu không phải full test
        Instant startTime,
        Instant endTime,
        Long durationSeconds      // tiện render thời gian
) {}

