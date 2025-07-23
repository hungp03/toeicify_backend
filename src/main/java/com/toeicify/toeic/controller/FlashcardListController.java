package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.flashcard.CreateFlashcardListRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardCreateRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardListUpdateRequest;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListDetailResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListResponse;
import com.toeicify.toeic.service.FlashcardListService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.toeicify.toeic.util.SecurityUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardListController {

    private final FlashcardListService flashcardListService;

    @GetMapping("/list")
    @ApiMessage("Get user lists flash card")
    public ResponseEntity<List<FlashcardListResponse>> getLists(@RequestParam String type) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(flashcardListService.getFlashcardLists(type, userId));
    }

    @PostMapping("/list")
    @ApiMessage("Create list flash card")
    public ResponseEntity<FlashcardListResponse> createList(@RequestBody CreateFlashcardListRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(flashcardListService.createFlashcardList(request, userId));
    }

    @GetMapping("/list/{id}")
    @ApiMessage("Get list flash card detail")
    public ResponseEntity<FlashcardListDetailResponse> getListDetail(@PathVariable Long id) {
        Long userId = null;
        try {
            userId = SecurityUtil.getCurrentUserId();
        } catch (Exception e) {
            // Không đăng nhập cũng cho xem nếu list public
        }
        return ResponseEntity.ok(flashcardListService.getFlashcardListDetail(id, userId));
    }

    @PostMapping("/{listId}/cards")
    @ApiMessage("Add flash card to List")
    public ResponseEntity<?> addFlashcard(
            @PathVariable Long listId,
            @RequestBody FlashcardCreateRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        flashcardListService.addFlashcardToList(listId, request, userId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{listId}/cards/{cardId}")
    @ApiMessage("Update flash card detail")
    public ResponseEntity<?> updateFlashcard(
            @PathVariable Long listId,
            @PathVariable Long cardId,
            @RequestBody FlashcardCreateRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        flashcardListService.updateFlashcardInList(listId, cardId, request, userId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{listId}/cards/{cardId}")
    @ApiMessage("Delete flash card from list")
    public ResponseEntity<?> deleteFlashcard(
            @PathVariable Long listId,
            @PathVariable Long cardId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        flashcardListService.deleteFlashcard(listId, cardId, userId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{listId}/toggle-public")
    @ApiMessage("Change list flash card status")
    public ResponseEntity<?> togglePublicStatus(@PathVariable Long listId) {
        Long userId = SecurityUtil.getCurrentUserId();
        boolean isNowPublic = flashcardListService.togglePublicStatus(listId, userId);
        return ResponseEntity.ok(Map.of("isPublic", isNowPublic));
    }
    @PutMapping("/list/{listId}")
    @ApiMessage("Update list flash card")
    public ResponseEntity<?> updateFlashcardList(
            @PathVariable Long listId,
            @RequestBody FlashcardListUpdateRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        flashcardListService.updateFlashcardList(listId, userId, request);
        return ResponseEntity.ok().build();
    }
}
