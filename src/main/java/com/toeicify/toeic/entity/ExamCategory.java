package com.toeicify.toeic.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Created by hungpham on 7/10/2025
 */
@Entity
@Table(name = "exam_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;

    @Column(name = "description")
    private String description;
}

