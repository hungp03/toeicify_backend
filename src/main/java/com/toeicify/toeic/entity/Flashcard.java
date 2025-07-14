package com.toeicify.toeic.entity;

/**
 * Created by hungpham on 7/14/2025
 */
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "flashcards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @ManyToOne
    @JoinColumn(name = "list_id", nullable = false)
    private FlashcardList list;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String frontText;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String backText;

    private String category;
    private Instant createdAt;
}

