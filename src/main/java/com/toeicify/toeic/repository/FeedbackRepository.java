package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by hungpham on 8/19/2025
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Page<Feedback> findByUser_UserId(Long userId, Pageable pageable);

}
