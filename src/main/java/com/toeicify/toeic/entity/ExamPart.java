package com.toeicify.toeic.entity;

import com.toeicify.toeic.util.enums.ToeicPartSpec;
import jakarta.persistence.*;
import lombok.*;


/**
 * Created by hungpham on 7/10/2025
 */
@Entity
@Table(name = "exam_parts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_id")
    private Long partId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "part_number", nullable = false)
    private Integer partNumber;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @Column(name = "description")
    private String description;

    @Column(name = "question_count", nullable = false)
    private Integer questionCount;

    /** Chuẩn TOEIC theo partNumber – không lưu DB */
    @Transient
    public int getExpectedQuestionCount() {
        return ToeicPartSpec.expectedFor(this.partNumber != null ? this.partNumber : 0);
    }

    /** Optional: chặn dữ liệu sai ngay tại entity */
    @PrePersist @PreUpdate
    private void validatePartNumber() {
        if (partNumber == null || !ToeicPartSpec.isValid(partNumber)) {
            throw new IllegalArgumentException("Invalid TOEIC partNumber: " + partNumber);
        }
        if (questionCount == null) questionCount = 0;
        if (questionCount < 0) questionCount = 0;
    }
}
