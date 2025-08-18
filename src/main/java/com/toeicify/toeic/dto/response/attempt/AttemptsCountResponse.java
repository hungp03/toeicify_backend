package com.toeicify.toeic.dto.response.attempt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttemptsCountResponse {
    private long totalAttempts;     // tổng lượt thi đã hoàn thành
    private long fullTests;         // số lượt full test đã hoàn thành
    private long practiceAttempts;  // số lượt luyện tập (theo part) đã hoàn thành
}