package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.StudySchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by hungpham on 8/31/2025
 */
@Repository
public interface StudyScheduleRepository extends JpaRepository<StudySchedule, Long> {
    @Query("SELECT s.user.userId FROM StudySchedule s WHERE s.scheduleId = :scheduleId")
    Long findOwnerIdByScheduleId(@Param("scheduleId") Long scheduleId);
    Optional<StudySchedule> findByScheduleIdAndUser_UserId(Long scheduleId, Long userId);
    Page<StudySchedule> findByUser_UserId(Long userId, Pageable pageable);
}
