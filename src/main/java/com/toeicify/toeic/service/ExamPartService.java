package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.response.exampart.MissingPartResponse;

import java.util.List;

public interface ExamPartService {
    void deleteExamPartById(Long partId);
    List<MissingPartResponse> getMissingPartsOfExam(Long examId);
}
