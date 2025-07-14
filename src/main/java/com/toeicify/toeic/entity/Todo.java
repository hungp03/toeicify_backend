package com.toeicify.toeic.entity;

/**
 * Created by hungpham on 7/14/2025
 */
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "todos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long todoId;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private StudySchedule schedule;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String taskDescription;

    private Boolean isCompleted = false;

    private LocalDate dueDate;
    private ZonedDateTime createdAt;
}

