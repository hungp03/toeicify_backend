package com.toeicify.toeic.dto.request.examcategory;

import com.toeicify.toeic.util.constant.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Created by hungpham on 7/10/2025
 */
public record ExamCategoryRequest(
        @NotBlank(message = "Category is required")
        @Pattern(
                regexp = ValidationPatterns.CATEGORY_PATTERN,
                message = "Category not valid. Only letters, digits, space, '_', '.', and '!' are allowed."
        )
        @Size(max = 255, message = "Description must be less than 255 characters")
        String categoryName,
        String description
) {
}
