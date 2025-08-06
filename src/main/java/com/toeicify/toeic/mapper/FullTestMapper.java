package com.toeicify.toeic.mapper;

import com.toeicify.toeic.dto.response.exam.ExamResponse;
import com.toeicify.toeic.dto.response.exampart.ExamPartResponse;
import com.toeicify.toeic.dto.response.question.QuestionGroupResponse;
import com.toeicify.toeic.dto.response.question.QuestionOptionResponse;
import com.toeicify.toeic.dto.response.question.QuestionResponse;
import com.toeicify.toeic.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FullTestMapper {
    @Mapping(source = "exam.examCategory.categoryId", target = "categoryId")
    @Mapping(source = "exam.examCategory.categoryName", target = "categoryName")
    @Mapping(source = "exam.createdBy.userId", target = "createdById")
    @Mapping(source = "exam.createdBy.username", target = "createdByName")
    @Mapping(source = "exam.status", target = "status")
    ExamResponse toExamResponse(Exam exam);

    @Mapping(source = "part.partId", target = "partId")
    @Mapping(source = "part.partNumber", target = "partNumber")
    @Mapping(source = "part.partName", target = "partName")
    @Mapping(source = "part.description", target = "description")
    @Mapping(source = "part.questionCount", target = "questionCount")
//    @Mapping(source = "part.questionGroups", target = "questionGroups")

    ExamPartResponse toExamPartResponse(ExamPart part);

    List<ExamPartResponse> toExamPartResponseList(List<ExamPart> parts);

    @Mapping(source = "part.partId", target = "partId")
    @Mapping(source = "part.partName", target = "partName")
    @Mapping(target = "questions", source = "questions")
    QuestionGroupResponse toQuestionGroupResponse(QuestionGroup group);

    @Mapping(target = "options", source = "options")
    @Mapping(source = "group.groupId", target = "groupId")
    QuestionResponse toQuestionResponse(Question question);

    QuestionOptionResponse toQuestionOptionResponse(QuestionOption questionOption);

    List<QuestionGroupResponse> toQuestionGroupResponseList(List<QuestionGroup> questionGroups);

    List<QuestionResponse> toQuestionResponseList(List<Question> questions);

    List<QuestionOptionResponse> toQuestionOptionResponseList(List<QuestionOption> options);
}
