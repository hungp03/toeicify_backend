package com.toeicify.toeic.dto.response.exam;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
/**
 * Created by hungpham on 8/9/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultDetailResponse {
    private Long attemptId;
    private Integer totalScore;
    private Integer listeningScore;
    private Integer readingScore;
    private Double completionTimeMinutes;
    private LocalDateTime submittedAt;
    private List<PartDetailResponse> partsDetail;
    private List<AnswerDetailResponse> answersDetail;
    private ExamSummaryResponse examSummary;
}
