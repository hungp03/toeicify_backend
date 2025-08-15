package com.toeicify.toeic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.toeicify.toeic.dto.request.question.QuestionGroupRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.question.QuestionExplainResponse;
import com.toeicify.toeic.dto.response.question.QuestionGroupResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Created by hungpham on 8/3/2025
 */
public interface QuestionService {
    QuestionGroupResponse createQuestionGroup(QuestionGroupRequest request);
    QuestionGroupResponse getQuestionGroupById(Long id);
    QuestionGroupResponse updateQuestionGroup(Long id, QuestionGroupRequest request);
    void deleteQuestionGroup(Long id);
    JsonNode getQuestionsByPartIds(List<Long> partIds);
    JsonNode getExamQuestionsByExam(Long examId);
    List<QuestionGroupResponse> getQuestionGroupsByPartId(Long partId);
    PaginationResponse searchQuestionGroups(Long partId, int page, int size);
    QuestionExplainResponse getExplain(Long questionId);
}
