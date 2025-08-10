package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.Question;
import com.toeicify.toeic.entity.QuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by hungpham on 8/3/2025
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE q.questionId = :id")
    Optional<Question> findByIdWithOptions(@Param("id") Long id);

    @Query("SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE q.group.groupId = :groupId " +
            "ORDER BY q.questionId")
    List<Question> findByGroupGroupIdWithOptions(@Param("groupId") Long groupId);

    List<Question> findByGroupGroupIdOrderByQuestionId(Long groupId);

    long countByGroupGroupId(Long groupId);

    // Đếm tổng số câu hỏi thuộc 1 part
    long countByGroupPartPartId(Long partId);

    // biết có tồn tại câu hỏi nào không (nhanh hơn COUNT)
    boolean existsByGroupPartPartId(Long partId);
}
