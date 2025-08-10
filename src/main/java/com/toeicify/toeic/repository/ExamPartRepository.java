package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.ExamPart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Created by hungpham on 7/10/2025
 */
public interface ExamPartRepository extends JpaRepository<ExamPart, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ExamPart p where p.partId = :id")
    Optional<ExamPart> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(p.questionCount), 0) from ExamPart p where p.exam.examId = :examId")
    int sumQuestionCountByExamId(@Param("examId") Long examId);
}
