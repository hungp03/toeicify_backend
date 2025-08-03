package com.toeicify.toeic.dto.request.flashcard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardCreateRequest {
    @NotBlank(message = "Front text is required")
    @Size(max = 255, message = "Front text cannot exceed 255 characters")
    private String frontText;

    @NotBlank(message = "Back text is required")
    @Size(max = 255, message = "Back text cannot exceed 255 characters")
    private String backText;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "noun|verb|adjective|adverb|preposition|conjunction|interjection|pronoun|article",
            message = "Category must be one of: noun, verb, adjective, adverb")
    private String category;
}