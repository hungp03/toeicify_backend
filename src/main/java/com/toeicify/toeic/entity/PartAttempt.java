package com.toeicify.toeic.entity;

/**
 * Created by hungpham on 7/14/2025
 */
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "part_attempts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "part_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partAttemptId;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private UserAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private ExamPart part;

    private Integer correct_percentage;

    private Instant startTime;
    private Instant endTime;
}

