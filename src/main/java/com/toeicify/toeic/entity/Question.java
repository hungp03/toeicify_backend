package com.toeicify.toeic.entity;

import com.toeicify.toeic.util.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.List;

/**
 * Created by hungpham on 7/14/2025
 */

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private QuestionGroup group;

    @Column(nullable = false)
    private Integer questionNumber;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(length = 10)
    private String correctAnswerOption;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @OrderBy("optionLetter ASC")
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options;
}
