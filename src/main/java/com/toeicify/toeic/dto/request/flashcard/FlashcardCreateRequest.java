package com.toeicify.toeic.dto.request.flashcard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FlashcardCreateRequest(
        @NotBlank(message = "Front text is required")
        @Size(max = 255, message = "Front text cannot exceed 255 characters")
        String frontText,

        @NotBlank(message = "Back text is required")
        @Size(max = 255, message = "Back text cannot exceed 255 characters")
        String backText,

        @NotBlank(message = "Category is required")
        @Pattern(
                regexp = "noun|verb|adjective|adverb|preposition|conjunction|interjection|pronoun|article",
                message = "Category must be one of: noun, verb, adjective, adverb"
        )
        String category
) {}
