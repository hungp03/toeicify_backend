package com.toeicify.toeic.service;

import com.toeicify.toeic.dto.request.flashcard.CreateFlashcardListRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardCreateRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardListUpdateRequest;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListDetailResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListResponse;

import java.util.List;

public interface FlashcardListService {
    List<FlashcardListResponse> getFlashcardLists(String type, Long userId);
    FlashcardListResponse createFlashcardList(CreateFlashcardListRequest request, Long userId);
    FlashcardListDetailResponse getFlashcardListDetail(Long listId, Long userId);
    void addFlashcardToList(Long listId, FlashcardCreateRequest request, Long userId);
    void updateFlashcardInList(Long listId, Long cardId, FlashcardCreateRequest request, Long userId);
    void deleteFlashcard(Long listId, Long cardId, Long userId);
    boolean togglePublicStatus(Long listId, Long userId);
    void updateFlashcardList(Long listId, Long userId, FlashcardListUpdateRequest request);
}
