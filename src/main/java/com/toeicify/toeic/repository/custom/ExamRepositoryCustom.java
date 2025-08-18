package com.toeicify.toeic.repository.custom;

import com.toeicify.toeic.dto.response.exam.ExamListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by hungpham on 7/20/2025
 */
public interface ExamRepositoryCustom {
    Page<ExamListItemResponse> searchExams(String keyword, Long categoryId, Pageable pageable, boolean onlyPublic);
}

