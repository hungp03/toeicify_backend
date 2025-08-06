package com.toeicify.toeic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.request.question.QuestionGroupRequest;
import com.toeicify.toeic.dto.request.question.QuestionRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.question.QuestionGroupListItemResponse;
import com.toeicify.toeic.dto.response.question.QuestionGroupResponse;
import com.toeicify.toeic.entity.ExamPart;
import com.toeicify.toeic.entity.Question;
import com.toeicify.toeic.entity.QuestionGroup;
import com.toeicify.toeic.entity.QuestionOption;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.mapper.QuestionMapper;
import com.toeicify.toeic.repository.ExamPartRepository;
import com.toeicify.toeic.repository.QuestionGroupRepository;
import com.toeicify.toeic.repository.QuestionRepository;
import com.toeicify.toeic.service.QuestionService;
import com.toeicify.toeic.util.validator.PartStructureValidator;
import com.toeicify.toeic.util.validator.QuestionGroupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 8/3/2025
 */


@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionGroupRepository questionGroupRepository;
    private final QuestionRepository questionRepository;
    private final ExamPartRepository examPartRepository;
    private final QuestionMapper questionMapper;
    private final QuestionGroupValidator questionGroupValidator;
    private final PartStructureValidator partStructureValidator;
    private final ObjectMapper objectMapper;
    @Override
    @Transactional
    public QuestionGroupResponse createQuestionGroup(QuestionGroupRequest request) {
        // Validate basic request structure
        questionGroupValidator.validateQuestionGroup(request);

        ExamPart examPart = examPartRepository.findById(request.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam part not found"));

        // Validate part-specific structure based on part number
        partStructureValidator.validatePartStructure(request, examPart.getPartNumber());

        QuestionGroup questionGroup = QuestionGroup.builder()
                .part(examPart)
                .passageText(request.passageText())
                .imageUrl(request.imageUrl())
                .audioUrl(request.audioUrl())
                .build();

        List<Question> questions = request.questions().stream()
                .map(questionReq -> createQuestionFromRequest(questionReq, questionGroup))
                .toList();

        questionGroup.setQuestions(questions);

        QuestionGroup savedGroup = questionGroupRepository.save(questionGroup);
        return questionMapper.toQuestionGroupResponse(savedGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionGroupResponse getQuestionGroupById(Long id) {
        // Step 1: Get QuestionGroup with Questions
        QuestionGroup questionGroup = questionGroupRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question group not found"));

        // Step 2: Get Questions with Options separately
        List<Question> questionsWithOptions = questionRepository.findByGroupGroupIdWithOptions(id);

        // Step 3: Set options to questions
        Map<Long, List<QuestionOption>> optionsByQuestionId = questionsWithOptions.stream()
                .collect(Collectors.toMap(
                        Question::getQuestionId,
                        q -> q.getOptions() != null ? q.getOptions() : new ArrayList<>()
                ));

        // Update questions with their options
        questionGroup.getQuestions().forEach(question -> {
            question.setOptions(optionsByQuestionId.get(question.getQuestionId()));
        });

        return questionMapper.toQuestionGroupResponse(questionGroup);
    }

    @Override
    @Transactional
    public QuestionGroupResponse updateQuestionGroup(Long id, QuestionGroupRequest request) {
        // Validate basic request structure
        questionGroupValidator.validateQuestionGroup(request);

        // Step 1: Get QuestionGroup with Questions
        QuestionGroup questionGroup = questionGroupRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question group not found"));

        ExamPart examPart = examPartRepository.findById(request.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam part not found"));

        // Validate part-specific structure based on part number
        partStructureValidator.validatePartStructure(request, examPart.getPartNumber());

        // Step 2: Get Questions with Options separately
        List<Question> questionsWithOptions = questionRepository.findByGroupGroupIdWithOptions(id);

        // Step 3: Set options to questions
        Map<Long, List<QuestionOption>> optionsByQuestionId = questionsWithOptions.stream()
                .collect(Collectors.toMap(
                        Question::getQuestionId,
                        q -> q.getOptions() != null ? q.getOptions() : new ArrayList<>()
                ));

        // Update questions with their options
        questionGroup.getQuestions().forEach(question -> {
            question.setOptions(optionsByQuestionId.get(question.getQuestionId()));
        });

        // Update group properties
        questionGroup.setPart(examPart);
        questionGroup.setPassageText(request.passageText());
        questionGroup.setImageUrl(request.imageUrl());
        questionGroup.setAudioUrl(request.audioUrl());

        // Handle questions update
        updateQuestionsForGroup(questionGroup, request.questions());

        QuestionGroup savedGroup = questionGroupRepository.save(questionGroup);
        return questionMapper.toQuestionGroupResponse(savedGroup);
    }

    @Override
    @Transactional
    public void deleteQuestionGroup(Long id) {
        if (!questionGroupRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question group not found");
        }
        questionGroupRepository.deleteById(id);
    }

    @Override
    public JsonNode getQuestionsByPartIds(List<Long> partIds) {
        if (partIds == null || partIds.isEmpty()) {
            throw new IllegalArgumentException("partIds must not be empty");
        }

        // Kiểm tra tất cả part thuộc cùng 1 đề thi
        List<Long> examIds = examPartRepository.findDistinctExamIdsByPartIds(partIds);
        if (examIds.size() != 1) {
            throw new IllegalArgumentException("All partIds must belong to the same exam");
        }

        Long[] partIdArray = partIds.toArray(new Long[0]);
        String json = questionRepository
                .getExamQuestionsByParts(partIdArray)
                .getFnGetExamQuestionsByParts();

        return parseJson(json);
    }



    @Override
    public JsonNode getExamQuestionsByExam(Long examId) {
        String json = questionRepository
                .getExamQuestionsByExam(examId)
                .getFnGetExamQuestionsByExam();

        return parseJson(json);
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid JSON from DB", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse searchQuestionGroups(Long partId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("groupId").ascending());
        Page<QuestionGroupListItemResponse> pageResult = questionGroupRepository.searchQuestionGroups(partId, pageable);
        return PaginationResponse.from(pageResult, pageable);
    }

    private Question createQuestionFromRequest(QuestionRequest request, QuestionGroup group) {
        Question question = Question.builder()
                .group(group)
                .questionText(request.questionText())
                .questionType(request.questionType())
                .correctAnswerOption(request.correctAnswerOption())
                .explanation(request.explanation())
                .build();

        List<QuestionOption> options = request.options().stream()
                .map(optionReq -> QuestionOption.builder()
                        .question(question)
                        .optionLetter(optionReq.optionLetter())
                        .optionText(optionReq.optionText())
                        .build())
                .toList();

        // Note: We need to add options to Question entity if there's a relationship
        // This depends on your Question entity structure

        return question;
    }

    private void updateQuestionsForGroup(QuestionGroup group, List<QuestionRequest> questionRequests) {
        // Get existing questions mapped by ID
        Map<Long, Question> existingQuestions = group.getQuestions().stream()
                .filter(q -> q.getQuestionId() != null)
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));

        // Get incoming question IDs
        Set<Long> incomingQuestionIds = questionRequests.stream()
                .map(QuestionRequest::questionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Remove questions not in the request
        group.getQuestions().removeIf(question ->
                question.getQuestionId() != null && !incomingQuestionIds.contains(question.getQuestionId()));

        for (QuestionRequest questionReq : questionRequests) {
            if (questionReq.questionId() != null) {
                // Update existing question
                Question existingQuestion = existingQuestions.get(questionReq.questionId());
                if (existingQuestion != null) {
                    updateExistingQuestion(existingQuestion, questionReq);
                } else {
                    throw new ResourceInvalidException("Question not found: ID " + questionReq.questionId());
                }
            } else {
                // Add new question
                Question newQuestion = createQuestionFromRequest(questionReq, group);
                group.getQuestions().add(newQuestion);
            }
        }
    }

    private void updateExistingQuestion(Question question, QuestionRequest request) {
        question.setQuestionText(request.questionText());
        question.setQuestionType(request.questionType());
        question.setCorrectAnswerOption(request.correctAnswerOption());
        question.setExplanation(request.explanation());

        // Update options if Question has options relationship
        // This part depends on your entity relationships
        // You might need to implement updateOptionsForQuestion method similar to updateQuestionsForGroup
    }
}