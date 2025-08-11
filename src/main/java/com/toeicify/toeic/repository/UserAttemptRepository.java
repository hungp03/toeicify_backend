package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.UserAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by hungpham on 8/9/2025
 */
@Repository
public interface UserAttemptRepository extends JpaRepository<UserAttempt, Long> {
    @Query(value = "SELECT * FROM submit_exam_and_calculate_score(" +
            ":userId, :examId, CAST(:answers AS jsonb), " +
            ":startTime, :endTime, :isFullTest, :partIds)",
            nativeQuery = true)
    List<Object[]> submitExamAndCalculateScore(
            @Param("userId") Long userId,
            @Param("examId") Long examId,
            @Param("answers") String answers,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("isFullTest") Boolean isFullTest,
            @Param("partIds") Long[] partIds
    );

    @Query(value = "SELECT get_attempt_detail_with_answers(:attemptId) as attempt_detail",
            nativeQuery = true)
    String getAttemptDetailWithAnswers(@Param("attemptId") Long attemptId);

    @Query(value = "SELECT * FROM get_parts_detail_by_attempt(:attemptId)",
            nativeQuery = true)
    List<Object[]> getPartsDetailByAttempt(@Param("attemptId") Long attemptId);

    @Query("SELECT ep.partId FROM ExamPart ep WHERE ep.exam.examId = :examId")
    List<Long> getPartIdsByExam(@Param("examId") Long examId);

    @Query("SELECT q.questionId FROM Question q " +
            "JOIN QuestionGroup qg ON q.group.groupId = qg.groupId " +
            "JOIN ExamPart ep ON qg.part.partId = ep.partId " +
            "WHERE ep.exam.examId = :examId")
    List<Long> getQuestionIdsByExam(@Param("examId") Long examId);

    @Query("SELECT q.questionId FROM Question q " +
            "JOIN QuestionGroup qg ON q.group.groupId = qg.groupId " +
            "WHERE qg.part.partId IN :partIds")
    List<Long> getQuestionIdsByParts(@Param("partIds") List<Long> partIds);

    @Query("SELECT ep.partId, ep.partNumber FROM ExamPart ep WHERE ep.partId IN :partIds")
    List<Object[]> getPartDetailsByIds(@Param("partIds") List<Long> partIds);

    @Query(value = """
    SELECT
      ua.attempt_id,
      ua.exam_id,
      e.exam_name,
      ua.start_time,
      ua.end_time,
      ua.is_full_test,
      ua.score,
      COALESCE(SUM(pa.score_part), 0) AS correct_count,
      COALESCE(SUM(
        CASE ep.part_number
          WHEN 1 THEN 6
          WHEN 2 THEN 25
          WHEN 3 THEN 39
          WHEN 4 THEN 30
          WHEN 5 THEN 30
          WHEN 6 THEN 16
          WHEN 7 THEN 54
          ELSE 0
        END
      ), 0) AS total_questions,
      COALESCE(STRING_AGG(ep.part_number::text, ',' ORDER BY ep.part_number), '') AS parts_text
    FROM user_attempts ua
    JOIN exams e ON e.exam_id = ua.exam_id
    LEFT JOIN part_attempts pa ON pa.attempt_id = ua.attempt_id
    LEFT JOIN exam_parts ep ON ep.part_id = pa.part_id
    WHERE ua.user_id = :userId
    GROUP BY ua.attempt_id, ua.exam_id, e.exam_name, ua.start_time, ua.end_time, ua.is_full_test, ua.score
    ORDER BY ua.end_time DESC NULLS LAST, ua.start_time DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM user_attempts ua
    WHERE ua.user_id = :userId
    """,
            nativeQuery = true)
    Page<Object[]> findAttemptHistory(@Param("userId") Long userId, Pageable pageable);

}
