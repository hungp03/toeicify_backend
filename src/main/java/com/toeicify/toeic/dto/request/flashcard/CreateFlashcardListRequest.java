package com.toeicify.toeic.dto.request.flashcard;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlashcardListRequest {
    private String listName;
    private String description;
}

