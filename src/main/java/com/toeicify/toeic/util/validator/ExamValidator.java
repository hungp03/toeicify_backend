package com.toeicify.toeic.util.validator;

import com.toeicify.toeic.dto.request.exam.AnswerRequest;
import com.toeicify.toeic.dto.request.exam.SubmitExamRequest;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.repository.UserAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 8/9/2025
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExamValidator {
    private final UserAttemptRepository userAttemptRepository;

    public void validateExamSubmission(SubmitExamRequest request) {
        // 1. Validate timing
        if (request.endTime().isBefore(request.startTime())) {
            throw new ResourceInvalidException("End time cannot be before start time");
        }

        // Validate completion time (reasonable limits)
        Duration duration = Duration.between(request.startTime(), request.endTime());
        long seconds = duration.toSeconds();

        if (seconds < 1) {
            throw new ResourceInvalidException("Completion time too short (minimum 1 second)");
        }

        // 2. Validate submit type and parts
        if ("partial".equals(request.submitType())) {
            if (request.partIds() == null || request.partIds().isEmpty()) {
                throw new ResourceInvalidException("Part IDs required for partial submission");
            }

            // Check if all partIds belong to the exam
            List<Long> validPartIds = userAttemptRepository.getPartIdsByExam(request.examId());
            if (validPartIds.isEmpty()) {
                throw new ResourceInvalidException("No parts found for exam " + request.examId());
            }

            for (Long partId : request.partIds()) {
                if (!validPartIds.contains(partId)) {
                    throw new ResourceInvalidException("Part ID " + partId + " does not belong to exam " + request.examId());
                }
            }

            // Log mixed submission for monitoring
            validateAndLogMixedSubmission(request.partIds(), request.examId());
        } else if ("full".equals(request.submitType())) {
            // For full test, partIds should be null or empty
            if (request.partIds() != null && !request.partIds().isEmpty()) {
                log.warn("Part IDs provided for full test submission - they will be ignored");
            }
        }

        // 3. Validate answers
        if (request.answers() == null || request.answers().isEmpty()) {
            throw new ResourceInvalidException("Answers cannot be empty");
        }

        if (request.answers().size() > 200) { // Max questions in TOEIC
            throw new ResourceInvalidException("Too many answers provided (maximum 200)");
        }

        // 4. Check for duplicate question IDs
        Set<Long> questionIds = request.answers().stream()
                .map(AnswerRequest::questionId)
                .collect(Collectors.toSet());

        if (questionIds.size() != request.answers().size()) {
            throw new ResourceInvalidException("Duplicate question IDs found");
        }

        // 5. Validate all questionIds belong to exam and submitted parts
        Set<Long> submittedQuestionIds = new HashSet<>(questionIds);
        Set<Long> validQuestionIds;

        if ("full".equals(request.submitType())) {
            // Full test: validate all questions belong to exam
            validQuestionIds = new HashSet<>(userAttemptRepository.getQuestionIdsByExam(request.examId()));

            if (validQuestionIds.isEmpty()) {
                throw new ResourceInvalidException("No questions found for exam " + request.examId());
            }

            // Check if all submitted questions belong to exam
            Set<Long> invalidQuestions = new HashSet<>(submittedQuestionIds);
            invalidQuestions.removeAll(validQuestionIds);

            if (!invalidQuestions.isEmpty()) {
                throw new ResourceInvalidException("Questions " + invalidQuestions + " do not belong to exam " + request.examId());
            }

        } else {
            // Partial test: validate questions belong to submitted parts
            validQuestionIds = new HashSet<>(userAttemptRepository.getQuestionIdsByParts(request.partIds()));

            if (validQuestionIds.isEmpty()) {
                throw new ResourceInvalidException("No questions found for parts " + request.partIds());
            }

            // Check if all submitted questions belong to submitted parts
            Set<Long> invalidQuestions = new HashSet<>(submittedQuestionIds);
            invalidQuestions.removeAll(validQuestionIds);

            if (!invalidQuestions.isEmpty()) {
                throw new ResourceInvalidException("Questions " + invalidQuestions + " do not belong to submitted parts " + request.partIds());
            }

            // Additional check: ensure no questions from other parts of the exam are submitted
            Set<Long> allExamQuestions = new HashSet<>(userAttemptRepository.getQuestionIdsByExam(request.examId()));
            Set<Long> questionsFromOtherParts = new HashSet<>(submittedQuestionIds);
            questionsFromOtherParts.retainAll(allExamQuestions);
            questionsFromOtherParts.removeAll(validQuestionIds);

            if (!questionsFromOtherParts.isEmpty()) {
                throw new ResourceInvalidException("Questions " + questionsFromOtherParts + " belong to exam " + request.examId() + " but not to submitted parts");
            }
        }

        // 6. Validate individual answer options
        for (AnswerRequest answer : request.answers()) {
            if (answer.questionId() == null) {
                throw new ResourceInvalidException("Question ID cannot be null");
            }

            if (answer.selectedOption() == null || answer.selectedOption().trim().isEmpty()) {
                throw new ResourceInvalidException("Selected option cannot be empty for question " + answer.questionId());
            }

            if (!answer.selectedOption().matches("^[A-D]$")) {
                throw new ResourceInvalidException("Invalid selected option '" + answer.selectedOption() + "' for question " + answer.questionId() + ". Must be A, B, C, or D");
            }
        }

        log.info("Exam submission validation passed for exam: {}, submit type: {}, {} questions",
                request.examId(), request.submitType(), request.answers().size());
    }

    private void validateAndLogMixedSubmission(List<Long> partIds, Long examId) {
        try {
            // Get part details to check if it's mixed listening/reading
            List<Object[]> partDetails = userAttemptRepository.getPartDetailsByIds(partIds);

            List<Integer> listeningParts = new ArrayList<>();
            List<Integer> readingParts = new ArrayList<>();

            for (Object[] row : partDetails) {
                int partNumber = ((Number) row[1]).intValue(); // Assuming part_number is at index 1
                if (partNumber >= 1 && partNumber <= 4) {
                    listeningParts.add(partNumber);
                } else if (partNumber >= 5 && partNumber <= 7) {
                    readingParts.add(partNumber);
                }
            }

            if (!listeningParts.isEmpty() && !readingParts.isEmpty()) {
                log.info("Mixed submission detected for exam {} - Listening parts: {}, Reading parts: {}",
                        examId, listeningParts, readingParts);
            } else if (!listeningParts.isEmpty()) {
                log.info("Listening-only submission for exam {} - Parts: {}", examId, listeningParts);
            } else if (!readingParts.isEmpty()) {
                log.info("Reading-only submission for exam {} - Parts: {}", examId, readingParts);
            }

        } catch (Exception e) {
            log.warn("Could not log mixed submission details for exam {}: {}", examId, e.getMessage());
        }
    }

    public void validateUserOwnsAttempt(Long userId, Long attemptId) {
        if (!userAttemptRepository.existsOwnedBy(attemptId, userId)) {
            throw new ResourceInvalidException("Attempt " + attemptId + " does not belong to user " + userId);
        }
    }
}
