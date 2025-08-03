package com.toeicify.toeic.dto.response.examcategory;

import lombok.*;

/**
 * Created by hungpham on 7/10/2025
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamCategoryResponse {
    private Long categoryId;
    private String categoryName;
    private String description;
    private Long examCount; // Thêm trường này
}
