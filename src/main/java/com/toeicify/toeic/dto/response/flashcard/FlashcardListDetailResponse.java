package com.toeicify.toeic.dto.response.flashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardListDetailResponse {
    private Long listId;
    private String listName;
    private String description;
    private Instant createdAt;
    private Boolean isPublic;
    private Boolean isOwner;

    private int totalCards;
    private int learnedCards;
    private int rememberedCards;
    private int needReviewCards;

    private List<FlashcardItem> flashcards;

    @Data
    @Builder
    public static class FlashcardItem {
        private Long cardId;
        private String frontText;
        private String backText;
        private String category;
        private Instant createdAt;
    }
}
