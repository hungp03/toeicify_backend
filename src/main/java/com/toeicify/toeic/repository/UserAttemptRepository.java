package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.UserAttempt;
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

    @Query("""
           select (count(ua) > 0)
           from UserAttempt ua
           where ua.attemptId = :attemptId
             and ua.user.userId = :userId
           """)
    boolean existsOwnedBy(@Param("attemptId") Long attemptId,
                          @Param("userId") Long userId);

    @Query(value = "SELECT get_user_progress(:userId, :limit)", nativeQuery = true)
    String getUserProgress(@Param("userId") Long userId, @Param("limit") Integer limit);
    List<UserAttempt> findTop1ByOrderByEndTimeDesc();

    // Gọi bản đủ tham số (có offset) để phân trang các attempt của user
    @Query(value = "SELECT find_attempt_history(:userId, :limit, :offset)", nativeQuery = true)
    String findAttemptHistoryJson(@Param("userId") Long userId,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    // (Tiện dụng) Gọi trang đầu (offset = 0)
    @Query(value = "SELECT find_attempt_history(:userId, :limit)", nativeQuery = true)
    String findAttemptHistoryFirstPage(@Param("userId") Long userId,
                                       @Param("limit") int limit);

    long countByEndTimeIsNotNull();
    long countByIsFullTestTrueAndEndTimeIsNotNull();
    long countByIsFullTestFalseAndEndTimeIsNotNull();
}
