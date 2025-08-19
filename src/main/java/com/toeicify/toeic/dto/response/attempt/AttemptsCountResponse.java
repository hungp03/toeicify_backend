package com.toeicify.toeic.dto.response.attempt;

public record AttemptsCountResponse(
        long totalAttempts,     // tổng lượt thi đã hoàn thành
        long fullTests,         // số lượt full test đã hoàn thành
        long practiceAttempts   // số lượt luyện tập (theo part) đã hoàn thành
) {}
