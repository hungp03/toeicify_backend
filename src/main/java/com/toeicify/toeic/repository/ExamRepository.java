package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.Exam;
import com.toeicify.toeic.projection.QuestionJsonProjection;
import com.toeicify.toeic.repository.custom.ExamRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Created by hungpham on 7/10/2025
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long>, ExamRepositoryCustom {
    boolean existsByExamName(String name);

    @EntityGraph(attributePaths = "examParts")
    Optional<Exam> findWithPartsByExamId(Long examId);

    @EntityGraph(attributePaths = "examParts")
    @Query("SELECT e FROM Exam e WHERE e.examId = :examId AND e.status = 'PUBLIC'")
    Optional<Exam> findPublicExamByIdPublic(Long examId);

    long countByExamCategory_CategoryId(Long categoryId);

    List<Exam> findTop1ByOrderByCreatedAtDesc();

    @Query(value = """
        SELECT DATE_TRUNC('month', e.created_at) as month, COUNT(e.exam_id) as exam_count
        FROM exams e
        WHERE e.created_at >= :start AND e.created_at < :end
        GROUP BY DATE_TRUNC('month', e.created_at)
        ORDER BY month DESC
    """, nativeQuery = true)
    List<Object[]> countExamsByMonth(@Param("start") Instant start, @Param("end") Instant end);
}
