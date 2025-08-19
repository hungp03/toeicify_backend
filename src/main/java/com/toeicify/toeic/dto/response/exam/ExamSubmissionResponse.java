package com.toeicify.toeic.dto.response.exam;

/**
 * Created by hungpham on 8/9/2025
 */
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ExamSubmissionResponse(
        Long attemptId,
        Integer totalScore,
        Integer listeningScore,
        Integer readingScore,
        Double completionTimeMinutes,
        LocalDateTime submittedAt,
        Integer totalQuestionsAnswered,
        Integer totalReadingInExam,
        Integer totalListeningInExam,
        Integer listeningQuestionsAnswered,
        Integer readingQuestionsAnswered,
        Integer totalQuestions,
        Integer totalListeningCorrect,
        Integer totalReadingCorrect,
        Integer totalCorrectAnswers,
        List<PartDetailResponse> partsDetail,
        ExamSummaryResponse examSummary
) {}


