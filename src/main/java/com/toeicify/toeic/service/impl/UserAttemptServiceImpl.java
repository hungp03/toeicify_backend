package com.toeicify.toeic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.request.exam.SubmitExamRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.attempt.AttemptItemResponse;
import com.toeicify.toeic.dto.response.attempt.AttemptsCountResponse;
import com.toeicify.toeic.dto.response.exam.*;
import com.toeicify.toeic.dto.response.stats.UserProgressResponse;
import com.toeicify.toeic.repository.UserAttemptRepository;
import com.toeicify.toeic.service.UserAttemptService;
import com.toeicify.toeic.util.SecurityUtil;
import com.toeicify.toeic.util.validator.ExamValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
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
    private final NotificationServiceImpl notificationService;

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

        notificationService.sendNotification(userId, "Hoàn thành bài thi", "Chúc mừng bạn đã hoàn thành bài thi");
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

    @Override
    public PaginationResponse getAttemptHistoryForCurrentUser(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        int limit = pageable.getPageSize();
        int offset = pageable.getPageNumber() * limit;

        // 1) Gọi function JSON từ DB
        //    - Trang bất kỳ: dùng bản 3 tham số (có offset)
        //    - (Nếu muốn trang đầu rõ ràng) có thể dùng findAttemptHistoryFirstPage(userId, limit)
        String payload = userAttemptRepository.findAttemptHistoryJson(userId, limit, offset);
        if (payload == null || payload.isBlank()) {
            // Trường hợp DB trả null (hiếm), trả về rỗng cho an toàn
            return PaginationResponse.from(
                    new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0),
                    pageable
            );
        }

        // 2) Parse JSON
        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (Exception e) {
            log.error("Cannot parse attempt history JSON payload: {}", payload, e);
            throw new RuntimeException("Cannot parse attempt history JSON", e);
        }

        JsonNode metaNode = root.path("meta");
        JsonNode resultNode = root.path("result");

        // 3) Map JSON -> List<AttemptHistoryRow> (attempt phẳng kèm examId/examName)
        List<AttemptHistoryRow> content = new ArrayList<>();
        if (resultNode.isArray()) {
            for (JsonNode item : resultNode) {
                long examId = item.path("examId").asLong();
                String examName = item.path("examName").asText();

                JsonNode a = item.path("attempt");
                AttemptItemResponse attempt = AttemptItemResponse.builder()
                        .attemptId(a.path("attemptId").asLong())
                        .fullTest(a.path("fullTest").asBoolean())
                        .parts(parseParts(a.path("parts")))
                        .correct(a.path("correct").asInt())
                        .total(a.path("total").asInt())
                        .toeicScore(a.path("toeicScore").isNull() ? null : a.path("toeicScore").asInt())
                        .startTime(parseInstant(a.path("startTime")))
                        .endTime(parseInstantNullable(a.path("endTime")))
                        .durationSeconds(a.path("durationSeconds").asLong())
                        .build();

                content.add(new AttemptHistoryRow(examId, examName, attempt));
            }
        }

        // 4) Lấy total từ meta -> dựng PageImpl để tái dùng PaginationResponse.from(...)
        long total = metaNode.path("total").asLong(0);
        Page<AttemptHistoryRow> page = new org.springframework.data.domain.PageImpl<>(content, pageable, total);

        return PaginationResponse.from(page, pageable);
    }

    @Override
    public AttemptsCountResponse getAttemptsCount() {
        long totalDone = userAttemptRepository.countByEndTimeIsNotNull();
        long fullDone = userAttemptRepository.countByIsFullTestTrueAndEndTimeIsNotNull();
        long practiceDone = userAttemptRepository.countByIsFullTestFalseAndEndTimeIsNotNull();

        return new AttemptsCountResponse(totalDone, fullDone, practiceDone);
    }



    /* Helpers */

    private static List<Integer> parseParts(JsonNode partsNode) {
        if (partsNode == null || partsNode.isNull() || !partsNode.isArray()) return List.of();
        List<Integer> parts = new ArrayList<>();
        for (JsonNode n : partsNode) parts.add(n.asInt());
        return parts;
    }

    private static Instant parseInstant(JsonNode node) {
        if (node == null || node.isNull()) return null;
        // to_jsonb(timestamptz) -> ISO-8601 có offset; Instant.parse chấp nhận
        return Instant.parse(node.asText());
    }

    private static Instant parseInstantNullable(JsonNode node) {
        return parseInstant(node);
    }

    @AllArgsConstructor
    @Getter
    static class AttemptHistoryRow {
        Long examId;
        String examName;
        AttemptItemResponse attempt;
    }


    // Private helper methods
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

    @Override
    public UserProgressResponse getUserProgress(int chartLimit) throws JsonProcessingException {
        Long userId = SecurityUtil.getCurrentUserId();
        String json = userAttemptRepository.getUserProgress(userId, chartLimit);
        JsonNode root = objectMapper.readTree(json);

        UserProgressResponse.Summary summary = new UserProgressResponse.Summary(
                root.path("summary").path("currentScore").asInt(0),
                root.path("summary").path("testsTaken").asInt(0),
                new BigDecimal(root.path("summary").path("studyHours").asText("0"))
        );

        UserProgressResponse.SectionHighs highs = new UserProgressResponse.SectionHighs(
                root.path("sectionHighs").path("listeningMax").asInt(0),
                root.path("sectionHighs").path("readingMax").asInt(0)
        );

        List<UserProgressResponse.TrendPoint> trend = new ArrayList<>();
        for (JsonNode n : root.path("scoreTrend")) {
            trend.add(new UserProgressResponse.TrendPoint(n.path("day").asText(), n.path("score").asInt()));
        }

        return new UserProgressResponse(summary, highs, trend);
    }

}
