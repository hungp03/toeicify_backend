package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.examcategory.ExamCategoryRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.examcategory.ExamCategoryResponse;
import com.toeicify.toeic.entity.ExamCategory;
import com.toeicify.toeic.service.ExamCategoryService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 7/10/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exam-categories")
public class ExamCategoryController {
    private final ExamCategoryService examCategoryService;

    @PostMapping
    @ApiMessage("Create new exam category")
    public ResponseEntity<ExamCategoryResponse> createExamCategory(@Valid @RequestBody ExamCategoryRequest examCategoryRequest) {
        ExamCategoryResponse createdCategory = examCategoryService.createExamCategory(examCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @GetMapping
    @ApiMessage("Get all exam categories")
    public ResponseEntity<PaginationResponse> getAllExamCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(examCategoryService.getAllExamCategories(page, size));
    }

    @GetMapping("/{categoryId}")
    @ApiMessage("Get a exam category")
    public ResponseEntity<ExamCategoryResponse> getExamCategoryById(@PathVariable Long categoryId) {
        ExamCategoryResponse categoryResponse = examCategoryService.getExamCategoryById(categoryId);
        return ResponseEntity.ok(categoryResponse);
    }

    @PutMapping("/{categoryId}")
    @ApiMessage("Update exam category")
    public ResponseEntity<ExamCategoryResponse> updateExamCategory(
            @PathVariable Long categoryId, @RequestBody ExamCategoryRequest examCategoryRequest) {
        ExamCategoryResponse updatedCategory = examCategoryService.updateExamCategory(categoryId, examCategoryRequest);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{categoryId}")
    @ApiMessage("Delete exam categories")
    public ResponseEntity<Void> deleteExamCategory(@PathVariable Long categoryId) {
            examCategoryService.deleteExamCategory(categoryId);
          return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @ApiMessage("Get all exam categories without pagination")
    public ResponseEntity<List<ExamCategoryResponse>> getAllExamCategoriesList() {
        List<ExamCategoryResponse> categories = examCategoryService.getAllExamCategoriesList();
        return ResponseEntity.ok(categories);
    }
}
