package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.feedback.FeedbackResponse;
import com.toeicify.toeic.entity.Feedback;
import com.toeicify.toeic.entity.FeedbackAttachment;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.CannotDeleteException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.mapper.FeedbackMapper;
import com.toeicify.toeic.repository.FeedbackAttachmentRepository;
import com.toeicify.toeic.repository.FeedbackRepository;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.FeedbackService;
import com.toeicify.toeic.service.NotificationService;
import com.toeicify.toeic.util.SecurityUtil;
import com.toeicify.toeic.util.enums.FeedbackStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Created by hungpham on 8/19/2025
 */
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final FeedbackAttachmentRepository attachmentRepository;
    private final FeedbackMapper feedbackMapper;
    private final NotificationService notificationService;

    @Override
    public FeedbackResponse createFeedback(String content, List<String> attachments) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Feedback feedback = Feedback.builder()
                .user(user)
                .content(content)
                .status(FeedbackStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        if (attachments != null && !attachments.isEmpty()) {
            List<FeedbackAttachment> atts = attachments.stream()
                    .map(url -> FeedbackAttachment.builder()
                            .feedback(saved)
                            .url(url)
                            .build())
                    .toList();
            attachmentRepository.saveAll(atts);
            saved.setAttachments(atts);
        }

        return feedbackMapper.toResponse(saved);
    }

    @Override
    public PaginationResponse getFeedbackByUser(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<Feedback> page = feedbackRepository.findByUser_UserId(userId, pageable);
        return PaginationResponse.from(page.map(feedbackMapper::toResponse), pageable); // map entity -> DTO
    }


    @Override
    public PaginationResponse getAllFeedback(Pageable pageable) {
        Page<Feedback> page = feedbackRepository.findAll(pageable);
        return PaginationResponse.from(page.map(feedbackMapper::toResponse), pageable);
    }


    @Override
    public FeedbackResponse updateFeedbackByAdmin(Long feedbackId, FeedbackStatus status, String adminNote) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));

        if (status != null) {
            feedback.setStatus(status);
            if (status == FeedbackStatus.PROCESSED) {
                feedback.setProcessedAt(LocalDateTime.now());
            }
        }

        if (adminNote != null) {
            feedback.setAdminNote(adminNote);
        }

        Feedback updated = feedbackRepository.save(feedback);
        notificationService.sendNotification(feedback.getUser().getUserId(), "Thông báo", "Quản trị viên vừa cập nhật góp ý của bạn. Bạn có thể xem tại lịch sử phản hồi");
        return feedbackMapper.toResponse(updated);
    }

    @Override
    public void deleteFeedback(Long feedbackId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));

        User fbUser = feedback.getUser();

        if ("ADMIN".equals(fbUser.getRole().getRoleId())
                && !Objects.equals(fbUser.getUserId(), userId)) {
            throw new CannotDeleteException("User not allowed to delete this feedback");
        }


        feedbackRepository.delete(feedback);
    }
}