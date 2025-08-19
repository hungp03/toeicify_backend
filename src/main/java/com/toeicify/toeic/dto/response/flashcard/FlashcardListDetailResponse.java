package com.toeicify.toeic.dto.response.flashcard;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record FlashcardListDetailResponse(
        Long listId,
        String listName,
        String description,
        Instant createdAt,
        Boolean isPublic,
        Boolean isOwner,
        Boolean inProgress,
        List<FlashcardItem> flashcards
) {
    @Builder
    public record FlashcardItem(
            Long cardId,
            String frontText,
            String backText,
            String category,
            Instant createdAt
    ) {}
}
