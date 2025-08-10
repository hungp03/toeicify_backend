package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.Exam;
import com.toeicify.toeic.repository.custom.ExamRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
/**
 * Created by hungpham on 7/10/2025
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long>, ExamRepositoryCustom {
    Page<Exam> findByExamNameContainingIgnoreCaseAndExamCategory_CategoryId(String keyword, Long categoryId,
                                                                            Pageable pageable);
    Page<Exam> findByExamNameContainingIgnoreCase(String keyword,
                                                  Pageable pageable);

    Page<Exam> findByExamCategory_CategoryId(Long categoryId,
                                             Pageable pageable);
    boolean existsByExamName(String name);

    @EntityGraph(attributePaths = "examParts")
    Optional<Exam> findWithPartsByExamId(Long examId);

    long countByExamCategory_CategoryId(Long categoryId);

    @Query("SELECT COUNT(e) FROM Exam e WHERE e.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    List<Exam> findTop1ByOrderByCreatedAtDesc();


}
