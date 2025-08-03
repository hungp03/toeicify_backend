package com.toeicify.toeic.projection;

import com.toeicify.toeic.entity.ExamCategory;

/**
 * Created by hungpham on 8/3/2025
 */
public interface ExamCategoryWithCount {
    ExamCategory getCategory();
    Long getExamCount();
}
