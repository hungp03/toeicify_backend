package com.toeicify.toeic.dto.request.flashcard;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardListUpdateRequest {
    @NotBlank(message = "List name is required")
    @Size(min = 3, max = 255, message = "List name must be between 1 and 255 characters")
    private String listName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotEmpty(message = "Flashcards cannot be empty")
    @Valid
    private List<FlashcardCreateRequest> flashcards;
}
