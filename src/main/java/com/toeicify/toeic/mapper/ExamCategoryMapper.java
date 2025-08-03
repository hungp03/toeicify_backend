package com.toeicify.toeic.mapper;

import com.toeicify.toeic.dto.request.examcategory.ExamCategoryRequest;
import com.toeicify.toeic.dto.response.examcategory.ExamCategoryResponse;
import com.toeicify.toeic.entity.ExamCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created by hungpham on 7/10/2025
 */
@Mapper(componentModel = "spring")
public interface ExamCategoryMapper {
    @Mapping(target = "examCount", ignore = true)
    ExamCategoryResponse toResponse(ExamCategory examCategory);
    ExamCategory toExamCategory(ExamCategoryRequest examCategoryRequest);
}
