package com.toeicify.toeic.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

/**
 * Created by hungpham on 7/14/2025
 */
@Entity
@Table(name = "user_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    private Instant startTime;
    private Instant endTime;

    private Integer score;

    private Boolean isFullTest;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartAttempt> partAttempts;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> userAnswers;
}
