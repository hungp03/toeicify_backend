package com.toeicify.toeic.repository;

import com.toeicify.toeic.dto.response.question.QuestionGroupListItemResponse;
import com.toeicify.toeic.entity.Question;
import com.toeicify.toeic.entity.QuestionGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
public interface QuestionGroupRepository extends JpaRepository<QuestionGroup, Long>{

    // Legacy JPQL methods (kept for backwards compatibility)
    @Query("SELECT DISTINCT qg FROM QuestionGroup qg " +
            "LEFT JOIN FETCH qg.questions " +
            "WHERE qg.groupId = :id")
    Optional<QuestionGroup> findByIdWithQuestions(@Param("id") Long id);

    @Query("SELECT new com.toeicify.toeic.dto.response.question.QuestionGroupListItemResponse(" +
            "qg.groupId, " +
            "qg.part.partId, " +
            "qg.part.partName, " +
            "SIZE(qg.questions), " +
            "qg.passageText, " +
            "qg.imageUrl, " +
            "qg.audioUrl) " +
            "FROM QuestionGroup qg " +
            "WHERE (:partId IS NULL OR qg.part.partId = :partId)")
    Page<QuestionGroupListItemResponse> searchQuestionGroups(@Param("partId") Long partId, Pageable pageable);

    @EntityGraph(attributePaths = { "part", "questions" })
    Optional<QuestionGroup> findWithGraphByGroupId(Long id);

    @EntityGraph(attributePaths = {"questions"})
    @Query("SELECT qg FROM QuestionGroup qg WHERE qg.part.partId = :partId ORDER BY qg.groupId")
    List<QuestionGroup> findByPartPartIdWithQuestions(@Param("partId") Long partId);


}
