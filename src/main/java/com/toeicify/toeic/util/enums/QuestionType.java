package com.toeicify.toeic.util.enums;

import lombok.Getter;

/**
 * Created by hungpham on 7/14/2025
 */
@Getter
public enum QuestionType {
    LISTENING_PHOTO("Listening - Photo"),
    LISTENING_QUESTION_RESPONSE("Listening - Question-Response"),
    LISTENING_CONVERSATION("Listening - Conversation"),
    LISTENING_TALK("Listening - Talk"),
    READING_INCOMPLETE_SENTENCES("Reading - Incomplete Sentences"),
    READING_TEXT_COMPLETION("Reading - Text Completion"),
    READING_SINGLE_PASSAGE("Reading - Single Passage"),
    READING_DOUBLE_PASSAGE("Reading - Double Passage"),
    READING_TRIPLE_PASSAGE("Reading - Triple Passage");
    private final String description;

    QuestionType(String description) {
        this.description = description;
    }

}
