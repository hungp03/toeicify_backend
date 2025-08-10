package com.toeicify.toeic.dto.response.exam;
import lombok.*;
/**
 * Created by hungpham on 8/9/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDetailResponse {
    private Long questionId;
    private String userAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private String explanation;
    private Integer partNumber;
}
