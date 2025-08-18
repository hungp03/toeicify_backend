package com.toeicify.toeic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeicify.toeic.dto.request.question.QuestionGroupRequest;
import com.toeicify.toeic.dto.request.question.QuestionOptionRequest;
import com.toeicify.toeic.dto.request.question.QuestionRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.question.QuestionExplainResponse;
import com.toeicify.toeic.dto.response.question.QuestionGroupListItemResponse;
import com.toeicify.toeic.dto.response.question.QuestionGroupResponse;
import com.toeicify.toeic.entity.*;
import com.toeicify.toeic.dto.response.question.QuestionOptionResponse;
import com.toeicify.toeic.entity.ExamPart;
import com.toeicify.toeic.entity.Question;
import com.toeicify.toeic.entity.QuestionGroup;
import com.toeicify.toeic.entity.QuestionOption;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.mapper.QuestionMapper;
import com.toeicify.toeic.repository.ExamPartRepository;
import com.toeicify.toeic.repository.ExamRepository;
import com.toeicify.toeic.repository.QuestionGroupRepository;
import com.toeicify.toeic.repository.QuestionRepository;
import com.toeicify.toeic.service.QuestionService;
import com.toeicify.toeic.util.validator.PartStructureValidator;
import com.toeicify.toeic.util.validator.QuestionGroupValidator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
    private final ExamRepository examRepository;
    private final QuestionMapper questionMapper;
    private final QuestionGroupValidator questionGroupValidator;
    private final PartStructureValidator partStructureValidator;
    private final ObjectMapper objectMapper;
    private final EntityManager em;
    @Override
    @Transactional
    public QuestionGroupResponse createQuestionGroup(QuestionGroupRequest request) {
        questionGroupValidator.validateQuestionGroup(request);

        ExamPart examPart = examPartRepository.findByIdForUpdate(request.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam part not found"));

        partStructureValidator.validatePartStructure(request, examPart.getPartNumber());

        final int expected = examPart.getExpectedQuestionCount();
        final int incoming = request.questions().size();
        final int current  = examPart.getQuestionCount() != null ? examPart.getQuestionCount() : 0;
        if (expected > 0 && (current + incoming) > expected) {
            throw new ResourceInvalidException("Adding " + incoming + " questions exceeds TOEIC limit for Part "
                    + examPart.getPartNumber() + " (" + expected + ").");
        }

        QuestionGroup group = QuestionGroup.builder()
                .part(examPart)
                .passageText(request.passageText())
                .imageUrl(request.imageUrl())
                .audioUrl(request.audioUrl())
                .build();

        List<Question> questions = request.questions().stream()
                .map(qr -> createQuestionFromRequest(qr, group))
                .toList();

        group.setQuestions(questions);
        questionGroupRepository.save(group);

        // 1) cập nhật questionCount của part
        examPart.setQuestionCount(current + questions.size());
        examPartRepository.save(examPart);   // <-- đảm bảo managed + dirty-checked
        em.flush();                          // <-- FLUSH trước khi SUM

        // 2) tính lại total dựa trên DB đã flush
        int total = examPartRepository.sumQuestionCountByExamId(examPart.getExam().getExamId());
        Exam exam = examPart.getExam();
        exam.setTotalQuestions(total);
        examRepository.save(exam);           // <-- lưu lại exam

        return questionMapper.toQuestionGroupResponse(group);
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
        questionGroupValidator.validateQuestionGroup(request);

        // ĐÃ đủ questions + options nhờ EntityGraph
        QuestionGroup questionGroup = questionGroupRepository.findWithGraphByGroupId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question group not found"));

        ExamPart examPart = examPartRepository.findById(request.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam part not found"));

        partStructureValidator.validatePartStructure(request, examPart.getPartNumber());
        // Update group fields
        questionGroup.setPart(examPart);
        questionGroup.setPassageText(request.passageText());
        questionGroup.setImageUrl(request.imageUrl());
        questionGroup.setAudioUrl(request.audioUrl());
        // Upsert questions + options trên collection managed
        int beforeCount = questionGroup.getQuestions() != null ? questionGroup.getQuestions().size() : 0;
        updateQuestionsForGroup(questionGroup, request.questions());
        int afterCount  = questionGroup.getQuestions() != null ? questionGroup.getQuestions().size() : 0;
        QuestionGroup savedGroup = questionGroupRepository.save(questionGroup);
        // CẬP NHẬT questionCount của Part (chênh lệch)
        int partCurrent = examPart.getQuestionCount() != null ? examPart.getQuestionCount() : 0;
        examPart.setQuestionCount(partCurrent + (afterCount - beforeCount));
        // CẬP NHẬT totalQuestions của Exam
        recalcExamTotalQuestions(examPart.getExam());
        return questionMapper.toQuestionGroupResponse(savedGroup);
    }

    @Override
    @Transactional
    public void deleteQuestionGroup(Long id) {
        QuestionGroup group = questionGroupRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question group not found"));
        ExamPart part = examPartRepository.findByIdForUpdate(group.getPart().getPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam part not found"));

        int removed = group.getQuestions() != null ? group.getQuestions().size() : 0;

        questionGroupRepository.deleteById(id);

        // cập nhật questionCount của part
        int current = part.getQuestionCount() != null ? part.getQuestionCount() : 0;
        part.setQuestionCount(Math.max(0, current - removed));

        // cập nhật tổng đề
        recalcExamTotalQuestions(part.getExam());
    }

    @Override
    @Cacheable(value = "toeicPart", keyGenerator = "toeicPartKeyGenerator")
    public JsonNode getQuestionsByPartIds(List<Long> partIds) {
        if (partIds == null || partIds.isEmpty()) {
            throw new ResourceInvalidException("partIds must not be empty");
        }

        // Kiểm tra tất cả part thuộc cùng 1 đề thi
        List<Long> examIds = examPartRepository.findDistinctExamIdsByPartIds(partIds);
        if (examIds.size() != 1) {
            throw new ResourceInvalidException("All partIds must belong to the same exam");
        }

        Long[] partIdArray = partIds.toArray(new Long[0]);
        String json = questionRepository
                .getExamQuestionsByParts(partIdArray)
                .getFnGetExamQuestionsByParts();

        return parseJson(json);
    }


//    @Override
//    @Transactional(readOnly = true)
//    public List<QuestionGroupResponse> getQuestionGroupsByPartId(Long partId) {
//        // Step 1: Get QuestionGroups with Questions
//        List<QuestionGroup> groups = questionGroupRepository.findByPartPartIdWithQuestions(partId);
//
//        // Step 2: Get all Questions with Options for this part
//        List<Question> allQuestionsWithOptions = groups.stream()
//                .flatMap(group -> questionRepository.findByGroupGroupIdWithOptions(group.getGroupId()).stream())
//                .toList();
//
//        // Step 3: Group options by question ID
//        Map<Long, List<QuestionOption>> optionsByQuestionId = allQuestionsWithOptions.stream()
//                .collect(Collectors.toMap(
//                        Question::getQuestionId,
//                        q -> q.getOptions() != null ? q.getOptions() : new ArrayList<>()
//                ));
//
//        // Step 4: Set options to questions
//        groups.forEach(group -> {
//            if (group.getQuestions() != null) {
//                group.getQuestions().forEach(question -> {
//                    question.setOptions(optionsByQuestionId.get(question.getQuestionId()));
//                });
//            }
//        });
//
//        return groups.stream()
//                .map(questionMapper::toQuestionGroupResponse)
//                .collect(Collectors.toList());
//    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionGroupResponse> getQuestionGroupsByPartId(Long partId) {
        List<QuestionGroup> groups = questionGroupRepository.findByPartPartIdWithQuestions(partId);
        return groups.stream()
                .map(questionMapper::toQuestionGroupResponse)
                .toList();
    }


    @Override
    @Cacheable(
            value = "toeicExam",
            key = "'examDetail:' + #examId",
            condition = "#examId != null"
    )
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
            throw new RuntimeException("Invalid JSON: ", e);
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
                .questionNumber(request.questionNumber())
                .questionType(request.questionType())
                .correctAnswerOption(request.correctAnswerOption())
                .explanation(request.explanation())
                .build();

//        List<QuestionOption> options = request.options().stream()
//                .map(optionReq -> QuestionOption.builder()
//                        .question(question)
//                        .optionLetter(optionReq.optionLetter())
//                        .optionText(optionReq.optionText())
//                        .build())
//                .toList();
        List<QuestionOption> opts = request.options().stream()
                .map(o -> {
                    QuestionOption opt = QuestionOption.builder()
                            .optionLetter(o.optionLetter())
                            .optionText(o.optionText())
                            .build();
                    opt.setQuestion(question); // <<< Owning side MUST be set
                    return opt;
                })
                .collect(Collectors.toList());   // <— mutable

        question.setOptions(opts); // set inverse side

        return question;
    }

    private void updateQuestionsForGroup(QuestionGroup group, List<QuestionRequest> questionRequests) {
        if (group.getQuestions() == null) {
            group.setQuestions(new ArrayList<>());
        }

        // Map of existing questions in the group
        Map<Long, Question> existingQuestions = group.getQuestions().stream()
                .filter(q -> q.getQuestionId() != null)
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));

        // Set of incoming question IDs from the request
        Set<Long> incomingQuestionIds = questionRequests.stream()
                .map(QuestionRequest::questionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Remove questions that are no longer in the request
        group.getQuestions().removeIf(q ->
                q.getQuestionId() != null && !incomingQuestionIds.contains(q.getQuestionId()));

        for (QuestionRequest qr : questionRequests) {
            if (qr.questionId() != null) {
                Question existing = existingQuestions.get(qr.questionId());
                if (existing == null) {
                    // Check if the question exists in the database
                    existing = questionRepository.findByQuestionId(qr.questionId())
                            .orElseThrow(() -> new ResourceInvalidException("Question not found: ID " + qr.questionId()));
                    // Associate the question with the group
                    existing.setGroup(group);
                    group.getQuestions().add(existing);
                }
                updateExistingQuestion(existing, qr); // Update the question with request data
            } else {
                // Create a new question
                Question created = createQuestionFromRequest(qr, group);
                group.getQuestions().add(created);
            }
        }
    }

    private void updateExistingQuestion(Question question, QuestionRequest request) {
        // fields
        question.setQuestionText(request.questionText());
        question.setQuestionType(request.questionType());
        question.setCorrectAnswerOption(request.correctAnswerOption());
        question.setExplanation(request.explanation());

        // === UPSERT OPTIONS ===
        upsertOptions(question, request.options());
    }

    private void upsertOptions(Question question, List<QuestionOptionRequest> reqOptions) {
        if (question.getOptions() == null) {
            question.setOptions(new ArrayList<>());
        }

        // map option hiện có theo id
        Map<Long, QuestionOption> existing = question.getOptions().stream()
                .filter(o -> o.getOptionId() != null)
                .collect(Collectors.toMap(QuestionOption::getOptionId, o -> o));

        // các id option sẽ giữ lại
        Set<Long> keepIds = reqOptions.stream()
                .map(QuestionOptionRequest::optionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // xoá option không còn trong request (orphanRemoval = true sẽ xoá DB)
        question.getOptions().removeIf(o -> o.getOptionId() != null && !keepIds.contains(o.getOptionId()));

        // upsert từng option theo request
        for (QuestionOptionRequest ro : reqOptions) {
            QuestionOption opt = (ro.optionId() != null) ? existing.get(ro.optionId()) : null;
            if (opt == null) {
                opt = new QuestionOption();
                opt.setQuestion(question);         // owning side MUST be set
                question.getOptions().add(opt);    // thêm vào collection managed
            }
            opt.setOptionLetter(ro.optionLetter());
            opt.setOptionText(ro.optionText());
        }
    }

    // Helper //
    private void recalcExamTotalQuestions(Exam exam) {
        int total = examPartRepository.sumQuestionCountByExamId(exam.getExamId());
        exam.setTotalQuestions(total);
    }

    @Override
    @Cacheable(value = "question", key = "#questionId")
    public QuestionExplainResponse getExplain(Long questionId) {
        Question q = questionRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));

        String audioUrl = (q.getGroup() != null) ? q.getGroup().getAudioUrl() : null;
        String imageUrl = (q.getGroup() != null) ? q.getGroup().getImageUrl() : null;

        List<QuestionOptionResponse> options = q.getOptions().stream()
                .sorted(Comparator.comparing(QuestionOption::getOptionLetter))
                .map(o -> new QuestionOptionResponse(
                        o.getOptionId(),
                        o.getOptionLetter(),
                        o.getOptionText()
                ))
                .toList();

        return new QuestionExplainResponse(
                q.getQuestionNumber(),
                q.getQuestionText(),
                audioUrl,
                imageUrl,
                q.getQuestionType(),
                q.getCorrectAnswerOption(),
                q.getExplanation(),
                options
        );
    }
}