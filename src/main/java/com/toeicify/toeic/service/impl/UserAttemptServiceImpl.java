package com.toeicify.toeic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.request.exam.SubmitExamRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.attempt.AttemptItemResponse;
import com.toeicify.toeic.dto.response.attempt.ExamHistoryResponse;
import com.toeicify.toeic.dto.response.exam.*;
import com.toeicify.toeic.repository.UserAttemptRepository;
import com.toeicify.toeic.service.UserAttemptService;
import com.toeicify.toeic.util.SecurityUtil;
import com.toeicify.toeic.util.validator.ExamValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

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
            // Validate user owns this attempt
            examValidator.validateUserOwnsAttempt(userId, attemptId);

            String resultJson = userAttemptRepository.getAttemptDetailWithAnswers(attemptId);
            JsonNode result = objectMapper.readTree(resultJson);

            return ExamResultDetailResponse.builder()
                    .attemptId(attemptId)
                    .totalScore(result.get("total_score").asInt())
                    .listeningScore(result.get("listening_score").asInt())
                    .readingScore(result.get("reading_score").asInt())
                    .completionTimeMinutes(result.get("completion_time").asDouble())
                    .submittedAt(LocalDateTime.parse(result.get("submitted_at").asText()))
                    .partsDetail(parsePartsDetail(result.get("parts_detail")))
                    .answersDetail(parseAnswersDetail(result.get("answers_detail")))
                    .examSummary(parseExamSummary(result.get("exam_summary")))
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Error parsing exam result JSON", e);
            throw new RuntimeException("Failed to get exam result", e);
        }
    }

    @Override
    public PaginationResponse getAttemptHistoryForCurrentUser(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();

        Page<Object[]> raw = userAttemptRepository.findAttemptHistory(userId, pageable);

        Page<AttemptHistoryRow> mapped = raw.map(r -> {
            int i = 0;
            Long attemptId  = ((Number) r[i++]).longValue();
            Long examId     = ((Number) r[i++]).longValue();
            String examName = (String) r[i++];

            Instant start   = toInstant(r[i++]);
            Instant end     = (r[i] == null) ? null : toInstant(r[i]); i++;

            Boolean isFull  = (Boolean) r[i++];
            Integer score   = (r[i] == null) ? null : ((Number) r[i]).intValue(); i++;
            Integer correct = ((Number) r[i++]).intValue();
            Integer totalQ  = ((Number) r[i++]).intValue();
            String partsTxt = (String) r[i++];

            List<Integer> parts = (partsTxt == null || partsTxt.isBlank())
                    ? List.of()
                    : Arrays.stream(partsTxt.split(",")).map(Integer::parseInt).toList();

            long durationSec = (end != null ? Duration.between(start, end).getSeconds() : 0);

            return new AttemptHistoryRow(
                    examId, examName,
                    AttemptItemResponse.builder()
                            .attemptId(attemptId)
                            .fullTest(isFull)
                            .parts(parts)
                            .correct(correct)
                            .total(totalQ)
                            .toeicScore(Boolean.TRUE.equals(isFull) ? score : null)
                            .startTime(start)
                            .endTime(end)
                            .durationSeconds(durationSec)
                            .build()
            );
        });

        // Truyền cả mapped Page và pageable vào from(...)
        return PaginationResponse.from(mapped, pageable);
    }


    // helper: chấp nhận nhiều kiểu thời gian
    private static Instant toInstant(Object v) {
        if (v == null) return null;
        if (v instanceof Instant i) return i;
        if (v instanceof java.sql.Timestamp ts) return ts.toInstant();
        if (v instanceof java.time.OffsetDateTime odt) return odt.toInstant();
        if (v instanceof java.time.LocalDateTime ldt) return ldt.toInstant(java.time.ZoneOffset.UTC);
        throw new IllegalArgumentException("Unsupported temporal type: " + v.getClass());
    }

    @AllArgsConstructor
    @Getter
    static class AttemptHistoryRow {
        Long examId;
        String examName;
        AttemptItemResponse attempt;
    }


    // Private helper methods
    private List<PartDetailResponse> parsePartsDetail(JsonNode partsNode) throws JsonProcessingException {
        if (partsNode == null || !partsNode.isArray()) return Collections.emptyList();

        List<PartDetailResponse> parts = new ArrayList<>();
        for (JsonNode partNode : partsNode) {
            parts.add(PartDetailResponse.builder()
                    .partNumber(partNode.get("part_number").asInt())
                    .partName(partNode.get("part_name").asText())
                    .correctAnswers(partNode.get("correct_answers").asInt())
                    .totalQuestions(partNode.get("total_questions").asInt())
                    .accuracyPercent(partNode.get("accuracy_percent").asDouble())
                    .build());
        }
        return parts;
    }

    private List<AnswerDetailResponse> parseAnswersDetail(JsonNode answersNode) throws JsonProcessingException {
        if (answersNode == null || !answersNode.isArray()) return Collections.emptyList();

        List<AnswerDetailResponse> answers = new ArrayList<>();
        for (JsonNode answerNode : answersNode) {
            answers.add(AnswerDetailResponse.builder()
                    .questionId(answerNode.get("question_id").asLong())
                    .userAnswer(answerNode.get("user_answer").asText())
                    .correctAnswer(answerNode.get("correct_answer").asText())
                    .isCorrect(answerNode.get("is_correct").asBoolean())
                    .explanation(answerNode.get("explanation").asText())
                    .partNumber(answerNode.get("part_number").asInt())
                    .build());
        }
        return answers;
    }

    private ExamSummaryResponse buildExamSummary(SubmitExamRequest request) {
        return ExamSummaryResponse.builder()
                .examId(request.examId())
                .submitType(request.submitType())
                .partsSubmitted(request.partIds())
                .build();
    }

    private ExamSummaryResponse parseExamSummary(JsonNode summaryNode) {
        if (summaryNode == null) return null;

        return ExamSummaryResponse.builder()
                .examId(summaryNode.get("exam_id").asLong())
                .examName(summaryNode.get("exam_name").asText())
                .submitType(summaryNode.get("submit_type").asText())
                .partsSubmitted(parsePartsList(summaryNode.get("parts_submitted")))
                .build();
    }

    private List<Long> parsePartsList(JsonNode partsNode) {
        if (partsNode == null || !partsNode.isArray()) return Collections.emptyList();

        List<Long> parts = new ArrayList<>();
        for (JsonNode partNode : partsNode) {
            parts.add(partNode.asLong());
        }
        return parts;
    }
}
