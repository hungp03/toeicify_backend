package com.toeicify.toeic.dto.response.question;

/**
 * Created by hungpham on 8/3/2025
 */
public record QuestionOptionResponse(
        Long optionId,
        String optionLetter,
        String optionText
) {}
