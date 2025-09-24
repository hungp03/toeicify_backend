package com.toeicify.toeic.dto.response.attempt;

public record AttemptsCountResponse(
        long totalAttempts, // total number of completed tests
        long fullTests, // number of completed full tests
        long practiceAttempts // number of completed practice tests (by part)
) {}
