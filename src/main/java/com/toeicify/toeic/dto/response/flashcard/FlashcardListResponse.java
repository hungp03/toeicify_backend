package com.toeicify.toeic.dto.response.flashcard;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardListResponse {
    private Long listId;
    private String listName;
    private int cardCount;
    private Instant createdAt;
    private String description;
    private String ownerName; // Chỉ dùng khi type = explore
}
