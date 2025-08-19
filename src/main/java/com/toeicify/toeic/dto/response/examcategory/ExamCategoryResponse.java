package com.toeicify.toeic.dto.response.examcategory;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Created by hungpham on 7/10/2025
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamCategoryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long categoryId;
    private String categoryName;
    private String description;
    private Long examCount;
}
