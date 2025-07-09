package com.toeicify.toeic.dto.request.examcategory;

import jakarta.validation.constraints.NotBlank;

/**
 * Created by hungpham on 7/10/2025
 */
public record ExamCategoryRequest(
        @NotBlank(message = "Category is required")
        String categoryName,
        String description
) {
}
