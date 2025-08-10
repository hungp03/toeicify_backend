package com.toeicify.toeic.util.validator;

/**
 * Created by hungpham on 8/3/2025
 */

import com.toeicify.toeic.dto.request.question.QuestionGroupRequest;
import com.toeicify.toeic.util.enums.QuestionType;
import com.toeicify.toeic.exception.ResourceInvalidException;
import org.springframework.stereotype.Component;

@Component
public class PartStructureValidator {

    public void validatePartStructure(QuestionGroupRequest request, Integer partNumber) {
        switch (partNumber) {
            case 1 -> validatePart1Structure(request);
            case 2 -> validatePart2Structure(request);
            case 3 -> validatePart3Structure(request);
            case 4 -> validatePart4Structure(request);
            case 5 -> validatePart5Structure(request);
            case 6 -> validatePart6Structure(request);
            case 7 -> validatePart7Structure(request);
            default -> throw new ResourceInvalidException("Invalid part number: " + partNumber);
        }
    }

    private void validatePart1Structure(QuestionGroupRequest request) {
        // Part 1: 1 question, có imageUrl + audioUrl, 4 options
        if (request.questions().size() != 1) {
            throw new ResourceInvalidException("Part 1 must have exactly 1 question per group");
        }

        if (request.imageUrl() == null || request.imageUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 1 must have imageUrl");
        }

        if (request.audioUrl() == null || request.audioUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 1 must have audioUrl");
        }

//        if (request.passageText() != null && !request.passageText().trim().isEmpty()) {
//            throw new ResourceInvalidException("Part 1 should not have passageText");
//        }

        // Validate question type
        QuestionType questionType = request.questions().get(0).questionType();
        if (questionType != QuestionType.LISTENING_PHOTO) {
            throw new ResourceInvalidException("Part 1 questions must be LISTENING_PHOTO type");
        }
    }

    private void validatePart2Structure(QuestionGroupRequest request) {
        // Part 2: 1 question, có audioUrl, 3 options
        if (request.questions().size() != 1) {
            throw new ResourceInvalidException("Part 2 must have exactly 1 question per group");
        }

        if (request.audioUrl() == null || request.audioUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 2 must have audioUrl");
        }

        if (request.imageUrl() != null && !request.imageUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 2 should not have imageUrl");
        }

//        if (request.passageText() != null && !request.passageText().trim().isEmpty()) {
//            throw new ResourceInvalidException("Part 2 should not have passageText");
//        }

        // Validate question type
        QuestionType questionType = request.questions().get(0).questionType();
        if (questionType != QuestionType.LISTENING_QUESTION_RESPONSE) {
            throw new ResourceInvalidException("Part 2 questions must be LISTENING_QUESTION_RESPONSE type");
        }
    }

    private void validatePart3Structure(QuestionGroupRequest request) {
        // Part 3: 3 questions, có audioUrl, 3 options each
        if (request.questions().size() != 3) {
            throw new ResourceInvalidException("Part 3 must have exactly 3 questions per group");
        }

        if (request.audioUrl() == null || request.audioUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 3 must have audioUrl");
        }

        if (request.imageUrl() != null && !request.imageUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 3 should not have imageUrl");
        }

//        if (request.passageText() != null && !request.passageText().trim().isEmpty()) {
//            throw new ResourceInvalidException("Part 3 should not have passageText");
//        }

        // Validate all questions are LISTENING_CONVERSATION type
        request.questions().forEach(question -> {
            if (question.questionType() != QuestionType.LISTENING_CONVERSATION) {
                throw new ResourceInvalidException("Part 3 questions must be LISTENING_CONVERSATION type");
            }
        });
    }

    private void validatePart4Structure(QuestionGroupRequest request) {
        // Part 4: 3 questions, có audioUrl, 3 options each
        if (request.questions().size() != 3) {
            throw new ResourceInvalidException("Part 4 must have exactly 3 questions per group");
        }

        if (request.audioUrl() == null || request.audioUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 4 must have audioUrl");
        }

        if (request.imageUrl() != null && !request.imageUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 4 should not have imageUrl");
        }

//        if (request.passageText() != null && !request.passageText().trim().isEmpty()) {
//            throw new ResourceInvalidException("Part 4 should not have passageText");
//        }

        // Validate all questions are LISTENING_TALK type
        request.questions().forEach(question -> {
            if (question.questionType() != QuestionType.LISTENING_TALK) {
                throw new ResourceInvalidException("Part 4 questions must be LISTENING_TALK type");
            }
        });
    }

    private void validatePart5Structure(QuestionGroupRequest request) {
        // Part 5: 1 question, không có audio/image/passage, 4 options
        if (request.questions().size() != 1) {
            throw new ResourceInvalidException("Part 5 must have exactly 1 question per group");
        }

        if (request.audioUrl() != null && !request.audioUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 5 should not have audioUrl");
        }

        if (request.imageUrl() != null && !request.imageUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 5 should not have imageUrl");
        }

        if (request.passageText() != null && !request.passageText().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 5 should not have passageText");
        }

        // Validate question type
        QuestionType questionType = request.questions().get(0).questionType();
        if (questionType != QuestionType.READING_INCOMPLETE_SENTENCES) {
            throw new ResourceInvalidException("Part 5 questions must be READING_INCOMPLETE_SENTENCES type");
        }
    }

    private void validatePart6Structure(QuestionGroupRequest request) {
        // Part 6: 4 questions, có passageText, có thể có imageUrl, 4 options each
        if (request.questions().size() != 4) {
            throw new ResourceInvalidException("Part 6 must have exactly 4 questions per group");
        }

        if (request.passageText() == null || request.passageText().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 6 must have passageText");
        }

        if (request.audioUrl() != null && !request.audioUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 6 should not have audioUrl");
        }

        // Validate all questions are READING_TEXT_COMPLETION type
        request.questions().forEach(question -> {
            if (question.questionType() != QuestionType.READING_TEXT_COMPLETION) {
                throw new ResourceInvalidException("Part 6 questions must be READING_TEXT_COMPLETION type");
            }
        });
    }

    private void validatePart7Structure(QuestionGroupRequest request) {
        // Part 7: 1-5 questions, có passageText, có thể có imageUrl, 4 options each
        int questionCount = request.questions().size();
        if (questionCount < 1 || questionCount > 5) {
            throw new ResourceInvalidException("Part 7 must have 1-5 questions per group");
        }
        // Có thể có ảnh của đoạn văn thay vì text

//        if (request.passageText() == null || request.passageText().trim().isEmpty()) {
//            throw new ResourceInvalidException("Part 7 must have passageText");
//        }

        if (request.audioUrl() != null && !request.audioUrl().trim().isEmpty()) {
            throw new ResourceInvalidException("Part 7 should not have audioUrl");
        }

        // Validate all questions are Part 7 reading types
        request.questions().forEach(question -> {
            QuestionType type = question.questionType();
            if (type != QuestionType.READING_SINGLE_PASSAGE &&
                    type != QuestionType.READING_DOUBLE_PASSAGE &&
                    type != QuestionType.READING_TRIPLE_PASSAGE) {
                throw new ResourceInvalidException("Part 7 questions must be reading passage types");
            }
        });
    }
}
