package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.feedback.AdminUpdateFeedbackRequest;
import com.toeicify.toeic.dto.request.feedback.FeedbackRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.feedback.FeedbackResponse;
import com.toeicify.toeic.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hungpham on 8/19/2025
 */
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(@RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(
                feedbackService.createFeedback(request.content(), request.attachments())
        );
    }

    @GetMapping("/user")
    public ResponseEntity<PaginationResponse> getFeedbackByUser(Pageable pageable) {
        return ResponseEntity.ok(feedbackService.getFeedbackByUser(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<PaginationResponse> getAllFeedback(Pageable pageable) {
        return ResponseEntity.ok(feedbackService.getAllFeedback(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FeedbackResponse> updateFeedbackByAdmin(
            @PathVariable Long id,
            @RequestBody AdminUpdateFeedbackRequest request
    ) {
        return ResponseEntity.ok(
                feedbackService.updateFeedbackByAdmin(id, request.status(), request.adminNote())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(
            @PathVariable Long id
    ) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}
