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
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Đề thi mới tạo mặc định đang chờ thêm câu hỏi
        exam.setStatus(ExamStatus.PENDING);

        Exam savedExam = examRepository.save(exam);
        return examMapper.toExamResponse(savedExam);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "toeicExam",
            key = "#id",
            unless = "#result == null || #result.status() != 'PUBLIC'"
    )
    public ExamResponse getExamById(Long id) {
        Exam exam = examRepository.findWithPartsByExamId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        return buildExamResponse(exam);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "toeicExam",
            key = "#id"
    )
    public ExamResponse getPublicExamById(Long id) {
        Exam exam = examRepository.findPublicExamByIdPublic(id)
                .orElseThrow(() -> new ResourceNotFoundException("Public exam not found"));
        return buildExamResponse(exam);
    }

    private ExamResponse buildExamResponse(Exam exam) {
        int total = exam.getExamParts() == null ? 0 :
                exam.getExamParts().stream()
                        .map(p -> p.getQuestionCount() == null ? 0 : p.getQuestionCount())
                        .mapToInt(Integer::intValue)
                        .sum();

        ExamResponse dto = examMapper.toExamResponse(exam);
        return new ExamResponse(
                dto.examId(),
                dto.examName(),
                dto.examDescription(),
                total,
                dto.listeningAudioUrl(),
                dto.status(),
                dto.createdAt(),
                dto.categoryId(),
                dto.categoryName(),
                dto.createdById(),
                dto.createdByName(),
                dto.examParts()
        );
    }


    @Transactional(readOnly = true)
    @Override
    public PaginationResponse searchExams(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ExamListItemResponse> pageResult = examRepository.searchExams(keyword, categoryId, pageable, false);
        return PaginationResponse.from(pageResult, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse searchExamsForClient(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ExamListItemResponse> pageResult = examRepository.searchExams(keyword, categoryId, pageable, true);
        return PaginationResponse.from(pageResult, pageable);
    }

    @Transactional
    @Override
    @CacheEvict(value = "toeicExam", key = "#id")
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = examRepository.findWithPartsByExamId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        ExamCategory category = examCategoryService.findExamCategoryById(request.categoryId());

        exam.setExamName(request.examName());
        exam.setExamDescription(request.examDescription());
        exam.setTotalQuestions(request.totalQuestions());
        exam.setListeningAudioUrl(request.listeningAudioUrl());
        exam.setExamCategory(category);

        Map<Long, ExamPart> existingParts = exam.getExamParts().stream()
                .filter(p -> p.getPartId() != null)
                .collect(Collectors.toMap(ExamPart::getPartId, p -> p));

        Set<Long> incomingPartIds = request.examParts().stream()
                .map(ExamPartRequest::partId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        exam.getExamParts().removeIf(part -> part.getPartId() != null && !incomingPartIds.contains(part.getPartId()));

        for (ExamPartRequest dto : request.examParts()) {
            if (dto.partId() != null) {
                ExamPart part = existingParts.get(dto.partId());
                if (part != null) {
                    part.setPartNumber(dto.partNumber());
                    part.setPartName(dto.partName());
                    part.setDescription(dto.description());
                    part.setQuestionCount(dto.questionCount());
                } else {
                    throw new ResourceNotFoundException("Part not found: ID " + dto.partId());
                }
            } else {
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

    @Override
    @Transactional
    @CacheEvict(value = "toeicExam", key = "#id")
    public ExamResponse updateStatus(Long id, ExamStatus newStatus) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        ExamStatus currentStatus = exam.getStatus();

        // ===== Logic chuyển đổi trạng thái =====
        // 1. CANCELLED -> PUBLIC: Không cho phép khôi phục trực tiếp đề đã hủy để public
        if (newStatus == ExamStatus.PUBLIC && currentStatus == ExamStatus.CANCELLED) {
            throw new ResourceInvalidException("Không thể công khai đề thi đã bị hủy");
        }

        // 2. Chuyển sang PUBLIC: chỉ cho phép khi đề đủ câu hỏi (vd: TOEIC 200 câu / 7 part)
        if ((newStatus == ExamStatus.PUBLIC) &&
                !isAllExistingPartsCompleted(exam)) {
            throw new ResourceInvalidException(
                    "Không thể chuyển từ " + currentStatus + " sang " + newStatus + " vì một số part chưa đủ số câu hỏi."
            );
        }

        // 3. Chuyển sang PENDING:
        // - Không cho phép nếu đề đã công khai (PUBLIC) hoặc đã đủ câu hỏi (coi như hoàn thiện)
        if (newStatus == ExamStatus.PENDING && currentStatus == ExamStatus.PUBLIC) {
            throw new ResourceInvalidException("Không thể chuyển đề thi công khai về trạng thái đang chờ. Nếu muốn chỉnh sửa đề vui lòng chuyển qua Riêng tư.");
        }
        if (newStatus == ExamStatus.PENDING &&
                currentStatus == ExamStatus.PRIVATE && isAllExistingPartsCompleted(exam)) {
            throw new ResourceInvalidException("Không thể chuyển đề đã đủ câu hỏi về trạng thái đang chờ.");
        }
        if (currentStatus == ExamStatus.CANCELLED && newStatus == ExamStatus.PENDING) {
            if (isAllExistingPartsCompleted(exam)) {
                throw new ResourceInvalidException("Không thể chuyển đề đã đủ câu hỏi về trạng thái đang chờ.. Nếu muốn chỉnh sửa đề vui lòng chuyển qua Riêng tư.");
            }
        }

        // 4. Chuyển từ CANCELLED -> PRIVATE:
        // - Cho phép khôi phục nhưng yêu cầu confirm (frontend nên có xác nhận)
        // - Không cần block trong backend vì đã yêu cầu confirm từ UI

        // 5. Các trường hợp khác (PRIVATE -> PUBLIC, PRIVATE -> CANCELLED, PENDING -> PRIVATE...)
        // - Cho phép tự do chuyển đổi

        exam.setStatus(newStatus);
        Exam savedExam = examRepository.save(exam);

        return examMapper.toExamResponse(savedExam);
    }

    // Kiểm tra exam đã đủ số lượng câu hỏi cần thiết chưa
    public boolean isAllExistingPartsCompleted(Exam exam) {
        if (exam.getExamParts() == null || exam.getExamParts().isEmpty()) return false;
        Map<Integer, Integer> requiredPerPart = Map.of(
                1, 6,
                2, 25,
                3, 39,
                4, 30,
                5, 30,
                6, 16,
                7, 54
        );

        for (ExamPart part : exam.getExamParts()) {
            int partNumber = part.getPartNumber();
            int actualCount = part.getQuestionCount();

            Integer requiredCount = requiredPerPart.get(partNumber);
            if (requiredCount == null) {
                continue;
            }

            if (actualCount < requiredCount) {
                return false;
            }
        }

        return true;
    }
}
