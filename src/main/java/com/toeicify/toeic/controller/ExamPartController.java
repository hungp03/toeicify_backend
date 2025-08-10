package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.response.exampart.MissingPartResponse;
import com.toeicify.toeic.service.ExamPartService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exam-parts")
public class ExamPartController {

    private final ExamPartService examPartService;

    @DeleteMapping("/{partId}")
    @ApiMessage("Delete exam part")
    public ResponseEntity<Void> deleteExamPartById(@PathVariable Long partId) {
        examPartService.deleteExamPartById(partId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/missing/{examId}")
    @ApiMessage("Get missing TOEIC parts for exam")
    public ResponseEntity<List<MissingPartResponse>> getMissingParts(@PathVariable Long examId) {
        return ResponseEntity.ok(examPartService.getMissingPartsOfExam(examId));
    }
}

