package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.FeedbackAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by hungpham on 8/19/2025
 */
public interface FeedbackAttachmentRepository extends JpaRepository<FeedbackAttachment, Long> {
}
