package com.toeicify.toeic.dto.response.question;

import java.io.Serial;
import java.io.Serializable;

/**
 * Created by hungpham on 8/3/2025
 */
public record QuestionOptionResponse(
        Long optionId,
        String optionLetter,
        String optionText
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
