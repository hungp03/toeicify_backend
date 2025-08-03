package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.flashcard.CreateFlashcardListRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardCreateRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardListUpdateRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListDetailResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListResponse;

public interface FlashcardService {
    PaginationResponse getFlashcardLists(String type, int page, int size);
    FlashcardListResponse createFlashcardList(CreateFlashcardListRequest request);
    FlashcardListDetailResponse getFlashcardListDetail(Long listId);
    PaginationResponse getPaginatedFlashcards(Long listId, int page, int size);
    void addFlashcardToList(Long listId, FlashcardCreateRequest request);
    void updateFlashcardInList(Long listId, Long cardId, FlashcardCreateRequest request);
    void deleteFlashcard(Long listId, Long cardId);
    boolean togglePublicStatus(Long listId);
    void updateFlashcardList(Long listId, FlashcardListUpdateRequest request);
    void markListInProgress(Long listId);
    void stopLearningList(Long listId);
    void deleteFlashcardList(Long listId);
    PaginationResponse searchFlashcardLists(String keyword, int page, int size);
}
