package com.toeicify.toeic.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.toeicify.toeic.dto.request.exam.ExamRequest;
import com.toeicify.toeic.dto.request.exam.SubmitExamRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.exam.ExamResponse;
import com.toeicify.toeic.dto.response.exam.ExamResultDetailResponse;
import com.toeicify.toeic.dto.response.exam.ExamSubmissionResponse;
import com.toeicify.toeic.service.ExamService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import com.toeicify.toeic.util.enums.ExamStatus;
import com.toeicify.toeic.service.UserAttemptService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hungpham on 7/10/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/exams")
public class ExamController {
    private final ExamService examService;
    private final UserAttemptService userAttemptService;

    @PostMapping
    @ApiMessage("Create exam")
    public ResponseEntity<ExamResponse> createExam(@Valid @RequestBody ExamRequest exam) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.createExam(exam));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get exam by id")
    public ResponseEntity<ExamResponse> getExamById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @GetMapping("/public/{id}")
    @ApiMessage("Get public exam by id")
    public ResponseEntity<ExamResponse> getPublicExamById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getPublicExamById(id));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update exam")
    public ResponseEntity<ExamResponse> updateExam(
            @PathVariable Long id,
            @Valid @RequestBody ExamRequest request
    ) {
        return ResponseEntity.ok(examService.updateExam(id, request));
    }

    @GetMapping
    @ApiMessage("Get exams")
    public ResponseEntity<PaginationResponse> searchExams(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(examService.searchExams(keyword, categoryId, page, size));
    }

    @GetMapping("/public")
    public ResponseEntity<PaginationResponse> searchExamsForClient(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(examService.searchExamsForClient(keyword, categoryId, page, size));
    }

    @DeleteMapping("{id}")
    @ApiMessage("Delete exam")
    public ResponseEntity<ExamResponse> deleteExam(@PathVariable Long id) {
        examService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/status")
    @ApiMessage("Update exam status")
    public ResponseEntity<ExamResponse> updateExamStatus(
            @PathVariable Long id,
            @RequestParam ExamStatus status
    ) {
        return ResponseEntity.ok(examService.updateStatus(id, status));
    }


    @PostMapping("submit")
    @ApiMessage("Submit exam")
    public ResponseEntity<ExamSubmissionResponse> submitExam(
            @RequestBody @Valid SubmitExamRequest request) throws JsonProcessingException {
        ExamSubmissionResponse response = userAttemptService.submitExam(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempts/{attemptId}/result")
    @ApiMessage("Get exam result")
    public ResponseEntity<ExamResultDetailResponse> getExamResult(
            @PathVariable @Positive Long attemptId) {
        ExamResultDetailResponse result = userAttemptService.getExamResult(attemptId);
        return ResponseEntity.ok(result);
    }
}
