package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.ExamPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by hungpham on 7/10/2025
 */
public interface ExamPartRepository extends JpaRepository<ExamPart, Long> {
    @Query("SELECT DISTINCT ep.exam.examId FROM ExamPart ep WHERE ep.partId IN :partIds")
    List<Long> findDistinctExamIdsByPartIds(@Param("partIds") List<Long> partIds);

}
