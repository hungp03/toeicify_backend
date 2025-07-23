package com.toeicify.toeic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, length = 64)
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "target_score")
    private Integer targetScore;

    @Column(name = "exam_date")
    private Instant examDate;

    @Column(name = "registration_date")
    private Instant registrationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnore // Ignore this field during serialization to avoid infinite recursion
    private Role role;

    @Column(name = "social_media_id", length = 100)
    private String socialMediaId;

    @Column(name = "social_media_provider", length = 100)
    private String socialMediaProvider;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudySchedule> studySchedules;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardList> flashcardLists;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAttempt> userAttempts;

    @PrePersist
    public void handleBeforeCreate() {
        isActive = Boolean.TRUE;
        registrationDate = Instant.now();
    }
}

