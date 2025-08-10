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
<<<<<<< HEAD
        Integer questionCount,
        Integer expectedQuestionCount
) {}
=======
        Integer questionCount
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}

>>>>>>> 40102ad05a4f349bf8574027fc2db5194fdbc961
