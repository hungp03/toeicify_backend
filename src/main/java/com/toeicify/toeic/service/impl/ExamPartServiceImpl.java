package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.response.exampart.MissingPartResponse;
import com.toeicify.toeic.entity.Exam;
import com.toeicify.toeic.entity.ExamPart;
import com.toeicify.toeic.exception.ResourceAlreadyExistsException;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.repository.ExamPartRepository;
import com.toeicify.toeic.repository.ExamRepository;
import com.toeicify.toeic.repository.QuestionGroupRepository;
import com.toeicify.toeic.repository.QuestionRepository;
import com.toeicify.toeic.service.ExamPartService;
import com.toeicify.toeic.util.enums.ToeicPartSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ExamPartServiceImpl implements ExamPartService {

    private final ExamPartRepository examPartRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    @Override
    public void deleteExamPartById(Long partId) {
        ExamPart part = examPartRepository.findByIdForUpdate(partId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam part not found"));

        Exam exam = part.getExam();
        if (exam == null) {
            throw new ResourceInvalidException("Exam part is not attached to any exam");
        }

        // 2) Kiểm tra ràng buộc: Part không có câu hỏi
        int qc = (part.getQuestionCount() == null) ? 0 : part.getQuestionCount();


        if (questionRepository.existsByGroupPartPartId(partId) ) {
            throw new ResourceAlreadyExistsException("Cannot delete part because it contains questions");
        }

        if (qc > 0) {
            throw new ResourceAlreadyExistsException("Cannot delete part because it contains questions");
        }

        // 3) Gỡ part khỏi exam
        exam.getExamParts().remove(part);

        // 4) Tính lại totalQuestions từ DB để chính xác
        int newTotal = examPartRepository.sumQuestionCountByExamId(exam.getExamId());
        exam.setTotalQuestions(newTotal);

        examRepository.saveAndFlush(exam);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MissingPartResponse> getMissingPartsOfExam(Long examId) {
        // Lấy exam kèm parts
        Exam exam = examRepository.findWithPartsByExamId(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        Set<Integer> existing = Optional.ofNullable(exam.getExamParts())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ExamPart::getPartNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // TOEIC: 1..7; chỉ lấy những part hợp lệ theo spec
        return IntStream.rangeClosed(1, 7)
                .filter(ToeicPartSpec::isValid)
                .filter(pn -> !existing.contains(pn))
                .mapToObj(pn -> new MissingPartResponse(
                        pn,
                        "Part " + pn,
                        ToeicPartSpec.expectedFor(pn)
                ))
                .sorted(Comparator.comparing(MissingPartResponse::partNumber))
                .collect(Collectors.toList());
    }
}

