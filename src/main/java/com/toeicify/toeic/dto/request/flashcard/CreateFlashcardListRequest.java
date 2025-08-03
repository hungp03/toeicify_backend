package com.toeicify.toeic.dto.request.flashcard;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlashcardListRequest {
    @NotBlank(message = "List name is required")
    @Size(min = 3, max = 255, message = "List name must be between 1 and 255 characters")
    private String listName;

    @Size(max = 500, message = "Description cannot exceed 255 characters")
    private String description;
}

