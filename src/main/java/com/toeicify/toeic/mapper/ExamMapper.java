package com.toeicify.toeic.mapper;

import com.toeicify.toeic.dto.response.exam.ExamListItemResponse;
import com.toeicify.toeic.dto.response.exam.ExamResponse;
import com.toeicify.toeic.dto.response.exampart.ExamPartResponse;
import com.toeicify.toeic.entity.Exam;
import com.toeicify.toeic.entity.ExamPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Created by hungpham on 7/10/2025
 */
@Mapper(componentModel = "spring")
public interface ExamMapper {

    @Mapping(source = "exam.examCategory.categoryId", target = "categoryId")
    @Mapping(source = "exam.examCategory.categoryName", target = "categoryName")
    @Mapping(source = "exam.createdBy.userId", target = "createdById")
    @Mapping(source = "exam.createdBy.username", target = "createdByName")
    @Mapping(source = "exam.status", target = "status")
    ExamResponse toExamResponse(Exam exam);

    @Mapping(source = "part.partId", target = "partId")
    @Mapping(target = "expectedQuestionCount",
            expression = "java(com.toeicify.toeic.util.enums.ToeicPartSpec.expectedFor(part.getPartNumber()))")
    ExamPartResponse toExamPartResponse(ExamPart part);

    List<ExamPartResponse> toExamPartResponseList(List<ExamPart> parts);

    @Mapping(source = "exam.examCategory.categoryName", target = "categoryName")
    @Mapping(target = "totalParts", expression = "java(exam.getExamParts() != null ? exam.getExamParts().size() : 0)")
    ExamListItemResponse toExamListItemResponse(Exam exam);
}
