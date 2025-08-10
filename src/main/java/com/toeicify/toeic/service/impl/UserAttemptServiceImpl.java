package com.toeicify.toeic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.request.exam.SubmitExamRequest;
import com.toeicify.toeic.dto.response.exam.*;
import com.toeicify.toeic.repository.UserAttemptRepository;
import com.toeicify.toeic.service.UserAttemptService;
import com.toeicify.toeic.util.SecurityUtil;
import com.toeicify.toeic.util.validator.ExamValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hungpham on 8/9/2025
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserAttemptServiceImpl implements UserAttemptService {
    private final UserAttemptRepository userAttemptRepository;
    private final ObjectMapper objectMapper;
    private final ExamValidator examValidator;

    @Override
    public ExamSubmissionResponse submitExam(SubmitExamRequest request) throws JsonProcessingException {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Processing exam submission for user: {}, exam: {}", userId, request.examId());

        // Validation
        examValidator.validateExamSubmission(request);

        // Convert answers to JSON
        String answersJson = objectMapper.writeValueAsString(request.answers());
        Boolean isFullTest = "full".equals(request.submitType());

        Long[] partIdsArray = null;
        if ("partial".equals(request.submitType()) && request.partIds() != null) {
            partIdsArray = request.partIds().toArray(new Long[0]);
        }

        // Call function
        List<Object[]> results = userAttemptRepository.submitExamAndCalculateScore(
                userId,
                request.examId(),
                answersJson,
                request.startTime(),
                request.endTime(),
                isFullTest,
                partIdsArray
        );

        // Map result từ Object[]
        if (results.isEmpty()) {
            throw new RuntimeException("No result returned from database");
        }

        Object[] result = results.getFirst();
        Long attemptId = ((Number) result[0]).longValue();
        Integer totalScore = ((Number) result[1]).intValue();
        Integer listeningScore = ((Number) result[2]).intValue();
        Integer readingScore = ((Number) result[3]).intValue();
        Double completionTime = ((Number) result[4]).doubleValue();
        LocalDateTime submittedAt = ((Timestamp) result[5]).toLocalDateTime();
        Integer totalQuestionsAnswered = ((Number) result[6]).intValue();
        Integer correctAnswers = ((Number) result[7]).intValue();
        Integer totalQuestionsInExam = ((Number) result[8]).intValue();
        Integer totalListeningInExam = ((Number) result[9]).intValue();
        Integer totalReadingInExam = ((Number) result[10]).intValue();
        Integer listeningQuestionsAnswered = ((Number) result[11]).intValue();
        Integer listeningCorrectAnswers = ((Number) result[12]).intValue();
        Integer readingQuestionsAnswered = ((Number) result[13]).intValue();
        Integer readingCorrectAnswers = ((Number) result[14]).intValue();

        List<PartDetailResponse> partsDetail = getPartsDetailByAttempt(attemptId);

        return ExamSubmissionResponse.builder()
                .attemptId(attemptId)
                .totalScore(totalScore)
                .listeningScore(listeningScore)
                .readingScore(readingScore)
                .completionTimeMinutes(completionTime)
                .submittedAt(submittedAt)
                .totalQuestions(totalQuestionsInExam)
                .totalQuestionsAnswered(totalQuestionsAnswered)
                .totalListeningInExam(totalListeningInExam)
                .totalReadingInExam(totalReadingInExam)
                .listeningQuestionsAnswered(listeningQuestionsAnswered)
                .readingQuestionsAnswered(readingQuestionsAnswered)
                .totalListeningCorrect(listeningCorrectAnswers)
                .totalReadingCorrect(readingCorrectAnswers)
                .totalCorrectAnswers(correctAnswers)
                .partsDetail(partsDetail)
                .examSummary(buildExamSummary(request))
                .build();
    }

    private ExamSummaryResponse buildExamSummary(SubmitExamRequest request) {
        return ExamSummaryResponse.builder()
                .examId(request.examId())
                .submitType(request.submitType())
                .partsSubmitted(request.partIds())
                .build();
    }

    private List<PartDetailResponse> getPartsDetailByAttempt(Long attemptId) {
        List<Object[]> results = userAttemptRepository.getPartsDetailByAttempt(attemptId);
        List<PartDetailResponse> partsDetail = new ArrayList<>();

        for (Object[] row : results) {
            partsDetail.add(PartDetailResponse.builder()
                    .partNumber(((Number) row[1]).intValue()) // part_number
                    .partName((String) row[2]) // part_name
                    .correctAnswers(((Number) row[3]).intValue()) // correct_answers
                    .totalQuestions(((Number) row[4]).intValue()) // total_questions
                    .accuracyPercent(((Number) row[5]).doubleValue()) // accuracy_percent
                    .build());
        }

        return partsDetail;
    }

    @Override
    public ExamResultDetailResponse getExamResult(Long attemptId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            examValidator.validateUserOwnsAttempt(userId, attemptId);

            String resultJson = userAttemptRepository.getAttemptDetailWithAnswers(attemptId);
            JsonNode result = objectMapper.readTree(resultJson);
            String startTime = result.findValue("startedAt").asText(null);
            LocalDateTime startedAt = startTime != null ? LocalDateTime.parse(startTime) : null;
            String submittedAtStr = result.path("submittedAt").asText(null);
            LocalDateTime submittedAt = submittedAtStr != null
                    ? LocalDateTime.parse(submittedAtStr) : null;

            return ExamResultDetailResponse.builder()
                    .attemptId(attemptId)
                    .isFullTest(result.path("isFullTest").asBoolean(false))
                    .totalScore(result.path("totalScore").asInt(0))
                    .listeningScore(result.path("listeningScore").asInt(0))
                    .readingScore(result.path("readingScore").asInt(0))
                    .startTime(startedAt)
                    .completionTimeMinutes(result.path("completionTime").asDouble(0.0))
                    .submittedAt(submittedAt)
                    .partsDetail(parsePartsDetail(result.path("partsDetail")))
                    .answersDetail(parseAnswersDetail(result.path("answersDetail")))
                    .examSummary(parseExamSummary(result.path("examSummary")))
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Error parsing exam result JSON", e);
            throw new RuntimeException("Failed to get exam result", e);
        }
    }

    private List<PartDetailResponse> parsePartsDetail(JsonNode partsNode) {
        if (partsNode == null || !partsNode.isArray()) return Collections.emptyList();

        List<PartDetailResponse> parts = new ArrayList<>();
        for (JsonNode partNode : partsNode) {
            parts.add(PartDetailResponse.builder()
                    .partNumber(partNode.path("partNumber").asInt(0))
                    .partName(partNode.path("partName").asText(""))
                    .correctAnswers(partNode.path("correctAnswers").asInt(0))
                    .totalQuestions(partNode.path("totalQuestions").asInt(0))
                    .accuracyPercent(partNode.path("accuracyPercent").asDouble(0.0))
                    .build());
        }
        return parts;
    }

    private List<AnswerDetailResponse> parseAnswersDetail(JsonNode answersNode) {
        if (answersNode == null || !answersNode.isArray()) return Collections.emptyList();

        List<AnswerDetailResponse> answers = new ArrayList<>();
        for (JsonNode answerNode : answersNode) {

            Boolean isCorrect = null;
            JsonNode ic = answerNode.get("isCorrect");
            if (ic != null && !ic.isNull()) {
                isCorrect = ic.asBoolean();
            }

            answers.add(AnswerDetailResponse.builder()
                    .questionId(answerNode.path("questionId").asLong(0L))
                    .questionNumber(answerNode.path("questionNumber").asInt(0))
                    .userAnswer(answerNode.path("userAnswer").asText(""))
                    .correctAnswer(answerNode.path("correctAnswer").asText(""))
                    .isCorrect(isCorrect)
                    .partNumber(answerNode.path("partNumber").asInt(0))
                    .build());
        }
        return answers;
    }

    private ExamSummaryResponse parseExamSummary(JsonNode summaryNode) {
        if (summaryNode == null || summaryNode.isMissingNode()) return null;

        return ExamSummaryResponse.builder()
                .examId(summaryNode.path("examId").asLong(0L))
                .examName(summaryNode.path("examName").asText(""))
                .submitType(summaryNode.path("submitType").asText(null))
                .partsSubmitted(parsePartsList(summaryNode.path("partsSubmitted")))
                .build();
    }

    private List<Long> parsePartsList(JsonNode partsNode) {
        if (partsNode == null || !partsNode.isArray()) return Collections.emptyList();
        List<Long> parts = new ArrayList<>();
        for (JsonNode n : partsNode) parts.add(n.asLong());
        return parts;
    }

}
