package com.toeicify.toeic.dto.request.flashcard;

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
    private String listName;
    private String description;
    private List<FlashcardCreateRequest> flashcards;
}
