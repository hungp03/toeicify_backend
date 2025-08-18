package com.toeicify.toeic.entity;

/**
 * Created by hungpham on 8/18/2025
 */
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_fcm_token",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "token"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

