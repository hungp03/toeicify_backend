package com.toeicify.toeic.dto.request.flashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardCreateRequest {
    private String frontText;
    private String backText;
    private String category;
}

