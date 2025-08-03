package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.request.exam.ExamRequest;
import com.toeicify.toeic.dto.request.exampart.ExamPartRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.exam.ExamListItemResponse;
import com.toeicify.toeic.dto.response.exam.ExamResponse;
import com.toeicify.toeic.entity.Exam;
import com.toeicify.toeic.entity.ExamCategory;
import com.toeicify.toeic.entity.ExamPart;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.ResourceAlreadyExistsException;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.mapper.ExamMapper;
import com.toeicify.toeic.repository.ExamRepository;
import com.toeicify.toeic.service.ExamCategoryService;
import com.toeicify.toeic.service.ExamService;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.SecurityUtil;
import com.toeicify.toeic.util.enums.ExamStatus;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 7/10/2025
 */
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {
    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final ExamCategoryService examCategoryService;
    private final UserService userService;

    @Override
    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        Long uid = SecurityUtil.getCurrentUserId();
        User user = userService.findById(uid);
        ExamCategory category = examCategoryService.findExamCategoryById(request.categoryId());
        if (examRepository.existsByExamName(request.examName())) {
            throw new ResourceAlreadyExistsException("Exam name already exists");
        }
        Exam exam = Exam.builder()
                .examName(request.examName())
                .examDescription(request.examDescription())
                .totalQuestions(request.totalQuestions())
                .listeningAudioUrl(request.listeningAudioUrl())
                .examCategory(category)
                .createdBy(user)
                .build();

        List<ExamPart> parts = request.examParts().stream()
                .map(p -> ExamPart.builder()
                        .exam(exam)
                        .partNumber(p.partNumber())
                        .partName(p.partName())
                        .description(p.description())
                        .questionCount(p.questionCount())
                        .build()
                ).toList();

        exam.setExamParts(parts);

        Exam savedExam = examRepository.save(exam);
        return examMapper.toExamResponse(savedExam);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResponse getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return examMapper.toExamResponse(exam);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginationResponse searchExams(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ExamListItemResponse> pageResult = examRepository.searchExams(keyword, categoryId, pageable);
        return PaginationResponse.from(pageResult, pageable);
    }

    @Transactional
    @Override
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = examRepository.findWithPartsByExamId(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        ExamCategory category = examCategoryService.findExamCategoryById(request.categoryId());

        exam.setExamName(request.examName());
        exam.setExamDescription(request.examDescription());
        exam.setTotalQuestions(request.totalQuestions());
        exam.setListeningAudioUrl(request.listeningAudioUrl());
        exam.setExamCategory(category);

        Map<Long, ExamPart> existingParts = exam.getExamParts().stream()
                .filter(p -> p.getPartId() != null)
                .collect(Collectors.toMap(ExamPart::getPartId, p -> p));

        // ID của các part từ request
        Set<Long> incomingPartIds = request.examParts().stream()
                .map(ExamPartRequest::partId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Xóa các part không nằm trong request
        exam.getExamParts().removeIf(part -> part.getPartId() != null && !incomingPartIds.contains(part.getPartId()));

        for (ExamPartRequest dto : request.examParts()) {
            if (dto.partId() != null) {
                // Sửa part cũ
                ExamPart part = existingParts.get(dto.partId());
                if (part != null) {
                    part.setPartNumber(dto.partNumber());
                    part.setPartName(dto.partName());
                    part.setDescription(dto.description());
                    part.setQuestionCount(dto.questionCount());
                } else {
                    throw new ResourceInvalidException("Part not found: ID " + dto.partId());
                }
            } else {
                // Thêm part mới
                ExamPart newPart = ExamPart.builder()
                        .exam(exam)
                        .partNumber(dto.partNumber())
                        .partName(dto.partName())
                        .description(dto.description())
                        .questionCount(dto.questionCount())
                        .build();
                exam.getExamParts().add(newPart);
            }
        }

        Exam updated = examRepository.save(exam);
        return examMapper.toExamResponse(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Exam exam = examRepository.findWithPartsByExamId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        boolean hasParts = exam.getExamParts() != null && !exam.getExamParts().isEmpty();
        if (hasParts) {
            exam.setStatus(ExamStatus.CANCELLED);
            examRepository.save(exam);
        } else {
            examRepository.delete(exam);
        }
    }
}
