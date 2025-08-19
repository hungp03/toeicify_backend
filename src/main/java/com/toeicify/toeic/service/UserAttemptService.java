package com.toeicify.toeic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.toeicify.toeic.dto.request.exam.SubmitExamRequest;
import com.toeicify.toeic.dto.response.attempt.AttemptsCountResponse;
import com.toeicify.toeic.dto.response.stats.ChartPracticePointData;
import com.toeicify.toeic.dto.response.exam.ExamResultDetailResponse;
import com.toeicify.toeic.dto.response.exam.ExamSubmissionResponse;
import com.toeicify.toeic.dto.response.stats.UserProgressResponse;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.attempt.ExamHistoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by hungpham on 8/9/2025
 */
public interface UserAttemptService {
    ExamSubmissionResponse submitExam(SubmitExamRequest request) throws JsonProcessingException;

    ExamResultDetailResponse getExamResult(Long attemptId);

    UserProgressResponse getUserProgress(int chartLimit) throws JsonProcessingException;

    public PaginationResponse getAttemptHistoryForCurrentUser(Pageable pageable);

    public AttemptsCountResponse getAttemptsCount();
}
