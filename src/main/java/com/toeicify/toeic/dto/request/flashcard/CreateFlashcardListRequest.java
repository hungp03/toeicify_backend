package com.toeicify.toeic.dto.request.flashcard;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record CreateFlashcardListRequest(
        @NotBlank(message = "List name is required")
        @Size(min = 3, max = 255, message = "List name must be between 1 and 255 characters")
        String listName,

        @Size(max = 500, message = "Description cannot exceed 255 characters")
        String description
) {}

