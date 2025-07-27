package com.toeicify.toeic.entity;

/**
 * Created by hungpham on 7/14/2025
 */
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "flashcard_lists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long listId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String listName;

    private Boolean isPublic = false;

    private Instant createdAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "in_progress")
    private Boolean inProgress = false;

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flashcard> flashcards;
}
