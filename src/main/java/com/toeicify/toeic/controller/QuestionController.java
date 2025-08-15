package com.toeicify.toeic.controller;
/**
 * Created by hungpham on 8/3/2025
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.toeicify.toeic.dto.request.question.QuestionGroupRequest;
import com.toeicify.toeic.dto.response.question.QuestionExplainResponse;
import com.toeicify.toeic.dto.response.question.QuestionGroupResponse;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question-groups")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionGroupResponse> createQuestionGroup(
            @Valid @RequestBody QuestionGroupRequest request) {
        QuestionGroupResponse response = questionService.createQuestionGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionGroupResponse> getQuestionGroupById(
            @PathVariable Long id) {
        QuestionGroupResponse response = questionService.getQuestionGroupById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionGroupResponse> updateQuestionGroup(
            @PathVariable Long id,
            @Valid @RequestBody QuestionGroupRequest request) {
        QuestionGroupResponse response = questionService.updateQuestionGroup(id, request);
        return ResponseEntity.ok( response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionGroup(@PathVariable Long id) {
        questionService.deleteQuestionGroup(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/by-part/{partId}")
    public ResponseEntity<List<QuestionGroupResponse>> getQuestionGroupsByPartId(
            @PathVariable Long partId) {
        List<QuestionGroupResponse> response = questionService.getQuestionGroupsByPartId(partId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-parts")
    public ResponseEntity<JsonNode> getQuestionsByPartIds(@RequestParam List<Long> partIds) {
        JsonNode result = questionService.getQuestionsByPartIds(partIds);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/by-exam/{id}")
    public ResponseEntity<JsonNode> getQuestionsByExamId(@PathVariable Long id) {
        JsonNode result = questionService.getExamQuestionsByExam(id);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/search")
    public ResponseEntity<PaginationResponse> searchQuestionGroups(
            @RequestParam(required = false) Long partId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationResponse response = questionService.searchQuestionGroups(partId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/explain/{id}")
    public ResponseEntity<QuestionExplainResponse> getQuestionExplain(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getExplain(id));
    }
}
