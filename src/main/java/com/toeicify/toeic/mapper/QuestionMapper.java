package com.toeicify.toeic.mapper;

import com.toeicify.toeic.dto.response.question.QuestionGroupResponse;
import com.toeicify.toeic.dto.response.question.QuestionOptionResponse;
import com.toeicify.toeic.dto.response.question.QuestionResponse;
import com.toeicify.toeic.entity.QuestionGroup;
import com.toeicify.toeic.entity.Question;
import com.toeicify.toeic.entity.QuestionOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "partId", source = "part.partId")
    @Mapping(target = "partName", source = "part.partName")
    @Mapping(target = "questions", source = "questions")
    QuestionGroupResponse toQuestionGroupResponse(QuestionGroup questionGroup);

    @Mapping(target = "options", source = "options")
    QuestionResponse toQuestionResponse(Question question);

    QuestionOptionResponse toQuestionOptionResponse(QuestionOption questionOption);

    List<QuestionGroupResponse> toQuestionGroupResponseList(List<QuestionGroup> questionGroups);

    List<QuestionResponse> toQuestionResponseList(List<Question> questions);

    List<QuestionOptionResponse> toQuestionOptionResponseList(List<QuestionOption> options);
}
