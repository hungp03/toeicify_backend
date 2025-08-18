package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.flashcard.CreateFlashcardListRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardCreateRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardListUpdateRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListDetailResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListResponse;
import com.toeicify.toeic.service.FlashcardService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardListService;

    @GetMapping("/list")
    @ApiMessage("Get flashcard lists")
    public ResponseEntity<PaginationResponse> getLists(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        return ResponseEntity.ok(flashcardListService.getFlashcardLists(type, page, size));
    }

    @GetMapping("/search")
    @ApiMessage("Search flashcard lists by name")
    public ResponseEntity<PaginationResponse> searchFlashcardLists(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        return ResponseEntity.ok(flashcardListService.searchFlashcardLists(keyword, page, size));
    }

    @PostMapping("/list")
    @ApiMessage("Create list flash card")
    public ResponseEntity<FlashcardListResponse> createList(@Valid @RequestBody CreateFlashcardListRequest request) {
        return ResponseEntity.ok(flashcardListService.createFlashcardList(request));
    }

    @GetMapping("/list/{id}")
    @ApiMessage("Get list flash card detail")
    public ResponseEntity<FlashcardListDetailResponse> getListDetail(@PathVariable Long id) {
        return ResponseEntity.ok(flashcardListService.getFlashcardListDetail(id));
    }

    @GetMapping("/list/{id}/cards")
    @ApiMessage("Get flashcards in a list")
    public ResponseEntity<PaginationResponse> getFlashcardsInList(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(flashcardListService.getPaginatedFlashcards(id, page, size));
    }


    @PostMapping("/{listId}/cards")
    @ApiMessage("Add flash card to List")
    public ResponseEntity<Void> addFlashcard(
            @PathVariable Long listId,
            @Valid @RequestBody FlashcardCreateRequest request
    ) {
        flashcardListService.addFlashcardToList(listId, request);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{listId}/cards/{cardId}")
    @ApiMessage("Update flash card detail")
    public ResponseEntity<Void> updateFlashcard(
            @PathVariable Long listId,
            @PathVariable Long cardId,
            @Valid @RequestBody FlashcardCreateRequest request
    ) {
        flashcardListService.updateFlashcardInList(listId, cardId, request);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{listId}/cards/{cardId}")
    @ApiMessage("Delete flash card from list")
    public ResponseEntity<Void> deleteFlashcard(
            @PathVariable Long listId,
            @PathVariable Long cardId
    ) {
        flashcardListService.deleteFlashcard(listId, cardId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{listId}/toggle-public")
    @ApiMessage("Change list flash card status")
    public ResponseEntity<?> togglePublicStatus(@PathVariable Long listId) {
        boolean isNowPublic = flashcardListService.togglePublicStatus(listId);
        return ResponseEntity.ok(Map.of("isPublic", isNowPublic));
    }
    @PutMapping("/list/{listId}")
    @ApiMessage("Update list flash card")
    public ResponseEntity<Void> updateFlashcardList(
            @PathVariable Long listId,
            @RequestBody FlashcardListUpdateRequest request
    ) {
        flashcardListService.updateFlashcardList(listId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/list/{listId}/start-learning")
    @ApiMessage("Mark list as in progress")
    public ResponseEntity<Void> startLearning(@PathVariable Long listId) {
        flashcardListService.markListInProgress(listId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/list/{listId}/stop-learning")
    @ApiMessage("Stop learning list")
    public ResponseEntity<Void> stopLearning(@PathVariable Long listId) {
        flashcardListService.stopLearningList(listId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/list/{listId}")
    @ApiMessage("Delete list and its flashcards")
    public ResponseEntity<Void> deleteFlashcardList(@PathVariable Long listId) {
        flashcardListService.deleteFlashcardList(listId);
        return ResponseEntity.ok().build();
    }
}
