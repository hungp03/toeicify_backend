package com.toeicify.toeic.dto.response.exam;

import java.util.List;
import lombok.Builder;
/**
 * Created by hungpham on 8/9/2025
 */
@Builder
public record ExamSummaryResponse(
        Long examId,
        String examName,
        String submitType,
        List<Long> partsSubmitted
) {}
