package com.toeicify.toeic.dto.response.exam;
import lombok.*;
/**
 * Created by hungpham on 8/9/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartDetailResponse {
    private Integer partNumber;
    private String partName;
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Double accuracyPercent;
}
