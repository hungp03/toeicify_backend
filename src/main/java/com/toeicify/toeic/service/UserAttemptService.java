package com.toeicify.toeic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.toeicify.toeic.dto.request.exam.SubmitExamRequest;
import com.toeicify.toeic.dto.response.exam.ExamResultDetailResponse;
import com.toeicify.toeic.dto.response.exam.ExamSubmissionResponse;

/**
 * Created by hungpham on 8/9/2025
 */
public interface UserAttemptService {
    ExamSubmissionResponse submitExam(SubmitExamRequest request) throws JsonProcessingException;

    ExamResultDetailResponse getExamResult(Long attemptId);
}
