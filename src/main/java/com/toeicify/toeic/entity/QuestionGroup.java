package com.toeicify.toeic.entity;

/**
 * Created by hungpham on 7/14/2025
 */
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "question_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private ExamPart part;

    @Column(columnDefinition = "TEXT")
    private String passageText;  // (Part 6, 7)

    private String imageUrl;     // (Part 7, 1)

    private String audioUrl;     // 3, 4

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}

