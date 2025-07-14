package com.toeicify.toeic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Created by hungpham on 7/14/2025
 */
@Entity
@Table(name = "question_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String optionLetter;  // A, B, C, D

    private String optionText;
}

