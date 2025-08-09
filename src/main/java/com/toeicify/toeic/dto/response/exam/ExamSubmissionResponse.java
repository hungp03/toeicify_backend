package com.toeicify.toeic.dto.response.exam;

/**
 * Created by hungpham on 8/9/2025
 */
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionResponse {
    private Long attemptId;
    private Integer totalScore;
    private Integer listeningScore;
    private Integer readingScore;
    private Double completionTimeMinutes;
    private LocalDateTime submittedAt;
    private Integer totalQuestionsAnswered;
    private Integer totalReadingInExam;
    private Integer totalListeningInExam;
    private Integer listeningQuestionsAnswered;
    private Integer readingQuestionsAnswered;
    private Integer totalQuestions;
    private Integer totalListeningCorrect;
    private Integer totalReadingCorrect;
    private Integer totalCorrectAnswers;
    private List<PartDetailResponse> partsDetail;
    private ExamSummaryResponse examSummary;
}

