package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.feedback.FeedbackResponse;
import com.toeicify.toeic.entity.Feedback;
import com.toeicify.toeic.util.enums.FeedbackStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by hungpham on 8/19/2025
 */
public interface FeedbackService {
    FeedbackResponse createFeedback(String content, List<String> attachments);
    PaginationResponse getFeedbackByUser(Pageable pageable);
    PaginationResponse getAllFeedback(Pageable pageable);
    FeedbackResponse updateFeedbackByAdmin(Long feedbackId, FeedbackStatus status, String adminNote);
    void deleteFeedback(Long feedbackId);
}

