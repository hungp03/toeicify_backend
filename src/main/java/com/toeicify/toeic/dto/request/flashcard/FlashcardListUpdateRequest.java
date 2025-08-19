package com.toeicify.toeic.dto.request.flashcard;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FlashcardListUpdateRequest(
        @NotBlank(message = "List name is required")
        @Size(min = 3, max = 255, message = "List name must be between 1 and 255 characters")
        String listName,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @NotEmpty(message = "Flashcards cannot be empty")
        @Valid
        List<FlashcardCreateRequest> flashcards
) {}

