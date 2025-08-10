// QuestionGroupValidator.java
package com.toeicify.toeic.util.validator;

import com.toeicify.toeic.dto.request.question.QuestionGroupRequest;
import com.toeicify.toeic.dto.request.question.QuestionRequest;
import com.toeicify.toeic.dto.request.question.QuestionOptionRequest;
import com.toeicify.toeic.util.enums.QuestionType;
import com.toeicify.toeic.exception.ResourceInvalidException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QuestionGroupValidator {

    public void validateQuestionGroup(QuestionGroupRequest request) {
        validateBasicStructure(request);
        validateQuestions(request.questions());
    }

    private void validateBasicStructure(QuestionGroupRequest request) {
        if (request.partId() == null) {
            throw new ResourceInvalidException("Part ID is required");
        }

        if (request.questions() == null || request.questions().isEmpty()) {
            throw new ResourceInvalidException("Question group must have at least one question");
        }

        // Validate URLs format if provided
        if (request.audioUrl() != null && !request.audioUrl().trim().isEmpty()) {
            validateUrlFormat(request.audioUrl(), "Audio URL");
        }

        if (request.imageUrl() != null && !request.imageUrl().trim().isEmpty()) {
            validateUrlFormat(request.imageUrl(), "Image URL");
        }
    }

    private void validateUrlFormat(String url, String fieldName) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ResourceInvalidException(fieldName + " must be a valid URL starting with http:// or https://");
        }
    }

    private void validateQuestions(List<QuestionRequest> questions) {
        for (int i = 0; i < questions.size(); i++) {
            try {
                validateSingleQuestion(questions.get(i));
            } catch (ResourceInvalidException e) {
                throw new ResourceInvalidException("Question " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    private void validateSingleQuestion(QuestionRequest question) {
        // Validate question type
        if (question.questionType() == null) {
            throw new ResourceInvalidException("Question type is required");
        }

        // Validate correct answer
        if (question.correctAnswerOption() == null || question.correctAnswerOption().trim().isEmpty()) {
            throw new ResourceInvalidException("Correct answer option is required");
        }

        // Validate options
        if (question.options() == null || question.options().isEmpty()) {
            throw new ResourceInvalidException("Question must have options");
        }

        // Validate question text for types that require it
        validateQuestionText(question);

        validateQuestionOptions(question.options(), question.correctAnswerOption(), question.questionType());
    }

    private void validateQuestionText(QuestionRequest question) {
        boolean requiresQuestionText = switch (question.questionType()) {
            case LISTENING_CONVERSATION, LISTENING_TALK,
                 READING_INCOMPLETE_SENTENCES, READING_TEXT_COMPLETION,
                 READING_SINGLE_PASSAGE, READING_DOUBLE_PASSAGE, READING_TRIPLE_PASSAGE -> true;
            case LISTENING_PHOTO, LISTENING_QUESTION_RESPONSE -> false;
        };

        if (requiresQuestionText && (question.questionText() == null || question.questionText().trim().isEmpty())) {
            throw new ResourceInvalidException("Question text is required for question type: " + question.questionType());
        }
    }

    private void validateQuestionOptions(List<QuestionOptionRequest> options, String correctAnswer, QuestionType questionType) {
        Set<String> optionLetters = options.stream()
                .map(QuestionOptionRequest::optionLetter)
                .collect(Collectors.toSet());

        // Determine expected number of options based on question type
        int expectedOptionCount = getExpectedOptionCount(questionType);
        Set<String> expectedOptions = getExpectedOptionLetters(questionType);

        // Validate option count
        if (options.size() != expectedOptionCount) {
            throw new ResourceInvalidException(
                    String.format("Question type %s must have exactly %d options, found %d",
                            questionType, expectedOptionCount, options.size()));
        }

        // Validate required option letters
        for (String expectedOption : expectedOptions) {
            if (!optionLetters.contains(expectedOption)) {
                throw new ResourceInvalidException(
                        String.format("Question type %s must include option %s",
                                questionType, expectedOption));
            }
        }

        // Validate that correct answer exists in options
        if (!optionLetters.contains(correctAnswer)) {
            throw new ResourceInvalidException(
                    String.format("Correct answer '%s' must match one of the option letters: %s",
                            correctAnswer, expectedOptions));
        }

        // Check for duplicate option letters
        if (options.size() != optionLetters.size()) {
            throw new ResourceInvalidException("Duplicate option letters found");
        }

        // Validate option text content
        for (QuestionOptionRequest option : options) {
            if (option.optionLetter() == null || option.optionLetter().trim().isEmpty()) {
                throw new ResourceInvalidException("Option letter cannot be empty");
            }

            if (option.optionText() == null || option.optionText().trim().isEmpty()) {
                throw new ResourceInvalidException("Option text cannot be empty for option " + option.optionLetter());
            }

            // Validate option letter format
            if (!option.optionLetter().matches("[ABCD]")) {
                throw new ResourceInvalidException("Option letter must be A, B, C, or D, found: " + option.optionLetter());
            }
        }
    }

//    private int getExpectedOptionCount(QuestionType questionType) {
//        return switch (questionType) {
//            case LISTENING_QUESTION_RESPONSE, LISTENING_CONVERSATION, LISTENING_TALK -> 3; // Part 2, 3, 4
//            case LISTENING_PHOTO, READING_INCOMPLETE_SENTENCES, READING_TEXT_COMPLETION,
//                 READING_SINGLE_PASSAGE, READING_DOUBLE_PASSAGE, READING_TRIPLE_PASSAGE -> 4; // Part 1, 5, 6, 7
//        };
//    }

//    private Set<String> getExpectedOptionLetters(QuestionType questionType) {
//        return switch (questionType) {
//            case LISTENING_QUESTION_RESPONSE, LISTENING_CONVERSATION, LISTENING_TALK ->
//                    Set.of("A", "B", "C"); // Part 2, 3, 4
//            case LISTENING_PHOTO, READING_INCOMPLETE_SENTENCES, READING_TEXT_COMPLETION,
//                 READING_SINGLE_PASSAGE, READING_DOUBLE_PASSAGE, READING_TRIPLE_PASSAGE ->
//                    Set.of("A", "B", "C", "D"); // Part 1, 5, 6, 7
//        };
//    }
    // Trong đề thi TOEIC chỉ có part 2 có 3 đáp án A-C
    private int getExpectedOptionCount(QuestionType questionType) {
        return switch (questionType) {
            case LISTENING_QUESTION_RESPONSE -> 3; // Part 2
            default -> 4; // Part 1, 2, 3, 4, 5, 6, 7
        };
    }

    private Set<String> getExpectedOptionLetters(QuestionType questionType) {
        return switch (questionType) {
            case LISTENING_QUESTION_RESPONSE ->
                    Set.of("A", "B", "C"); // Part 2
            default ->  Set.of("A", "B", "C", "D"); // Part 1, 3, 4, 5, 6, 7
        };
    }

    public void validateCorrectAnswerOption(String correctAnswer, QuestionType questionType) {
        Set<String> validOptions = getExpectedOptionLetters(questionType);
        if (!validOptions.contains(correctAnswer)) {
            throw new ResourceInvalidException(
                    String.format("Correct answer '%s' is not valid for question type %s. Valid options: %s",
                            correctAnswer, questionType, validOptions));
        }
    }

    // Utility method to validate individual option
    public void validateOption(QuestionOptionRequest option, QuestionType questionType) {
        if (option == null) {
            throw new ResourceInvalidException("Option cannot be null");
        }

        Set<String> validLetters = getExpectedOptionLetters(questionType);
        if (!validLetters.contains(option.optionLetter())) {
            throw new ResourceInvalidException(
                    String.format("Option letter '%s' is not valid for question type %s. Valid letters: %s",
                            option.optionLetter(), questionType, validLetters));
        }

        if (option.optionText() == null || option.optionText().trim().isEmpty()) {
            throw new ResourceInvalidException("Option text cannot be empty");
        }
    }

    // Method to get validation summary for a question type
    public String getValidationRules(QuestionType questionType) {
        int optionCount = getExpectedOptionCount(questionType);
        Set<String> validLetters = getExpectedOptionLetters(questionType);

        return String.format("Question type %s requires:\n" +
                        "- Exactly %d options\n" +
                        "- Option letters: %s\n" +
                        "- Correct answer must be one of: %s",
                questionType, optionCount, validLetters, validLetters);
    }
}