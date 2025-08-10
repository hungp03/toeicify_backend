package com.toeicify.toeic.dto.response.exampart;

import java.io.Serial;
import java.io.Serializable;

/**
 * Created by hungpham on 7/10/2025
 */
public record ExamPartResponse(
        Long partId,
        Integer partNumber,
        String partName,
        String description,
        Integer questionCount,
        Integer expectedQuestionCount
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
