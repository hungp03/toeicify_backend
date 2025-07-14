package com.toeicify.toeic.entity;

/**
 * Created by hungpham on 7/14/2025
 */
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "study_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos;
}
