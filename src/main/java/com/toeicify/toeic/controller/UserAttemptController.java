package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.attempt.AttemptsCountResponse;
import com.toeicify.toeic.dto.response.attempt.ExamHistoryResponse;
import com.toeicify.toeic.service.UserAttemptService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class UserAttemptController {
    private final UserAttemptService userAttemptService;

    @GetMapping("/history")
    @ApiMessage("Get attempt history")
    public ResponseEntity<PaginationResponse> getMyAttemptHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        return ResponseEntity.ok(userAttemptService.getAttemptHistoryForCurrentUser(pageable));
    }
    @GetMapping("/attempts-count")
    public ResponseEntity<AttemptsCountResponse> getAttemptsCount() {
        return ResponseEntity.ok(userAttemptService.getAttemptsCount());
    }
}
