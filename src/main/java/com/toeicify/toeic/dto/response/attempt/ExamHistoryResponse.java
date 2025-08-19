package com.toeicify.toeic.dto.response.attempt;

import java.util.List;

public record ExamHistoryResponse(
        Long examId,
        String examName,
        List<AttemptItemResponse> attempts
) {}
