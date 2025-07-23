package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.request.flashcard.CreateFlashcardListRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardCreateRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardListUpdateRequest;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListDetailResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListResponse;
import com.toeicify.toeic.entity.Flashcard;
import com.toeicify.toeic.entity.FlashcardList;
import com.toeicify.toeic.entity.FlashcardProgress;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.mapper.FlashcardListMapper;
import com.toeicify.toeic.repository.FlashcardListRepository;
import com.toeicify.toeic.repository.FlashcardProgressRepository;
import com.toeicify.toeic.repository.FlashcardRepository;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.FlashcardListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardListServiceImpl implements FlashcardListService {

    private final FlashcardListRepository flashcardListRepository;
    private final FlashcardProgressRepository progressRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final FlashcardListMapper flashcardListMapper;

    @Override
    public List<FlashcardListResponse> getFlashcardLists(String type, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        return switch (type) {
            case "mine" -> flashcardListRepository.findByUser_UserId(userId).stream()
                    .map(flashcardListMapper::toResponse).toList();
            case "learning" -> flashcardListRepository.findLearningByUserId(userId).stream()
                    .map(flashcardListMapper::toResponse).toList();
            case "explore" -> flashcardListRepository.findPublicFlashcardsExcludingUser(userId).stream()
                    .map(list -> {
                        FlashcardListResponse dto = flashcardListMapper.toResponse(list);
                        dto.setOwnerName(list.getUser().getFullName());
                        return dto;
                    }).toList();
            default -> throw new IllegalArgumentException("Invalid type param: " + type);
        };
    }

    @Override
    public FlashcardListResponse createFlashcardList(CreateFlashcardListRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        FlashcardList list = FlashcardList.builder()
                .listName(request.getListName())
                .description(request.getDescription())
                .user(user)
                .isPublic(false) // mặc định là private
                .createdAt(Instant.now())
                .build();
        return flashcardListMapper.toResponse(flashcardListRepository.save(list));
    }

    @Override
    public FlashcardListDetailResponse getFlashcardListDetail(Long listId, Long userId) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        List<FlashcardListDetailResponse.FlashcardItem> cardItems = list.getFlashcards().stream()
                .sorted(Comparator.comparing(Flashcard::getCardId))
                .map(card -> FlashcardListDetailResponse.FlashcardItem.builder()
                        .cardId(card.getCardId())
                        .frontText(card.getFrontText())
                        .backText(card.getBackText())
                        .category(card.getCategory())
                        .createdAt(card.getCreatedAt())
                        .build())
                .toList();

        int total = cardItems.size();
        int learned = 0;
        int remembered = 0;
        int review = 0;

        if (userId != null) {
            Optional<FlashcardProgress> progressOpt = progressRepository.findByUser_UserIdAndList_ListId(userId, listId);
            if (progressOpt.isPresent()) {
                FlashcardProgress progress = progressOpt.get();
                learned = progress.getCorrectCount() + progress.getWrongCount();
                remembered = progress.getCorrectCount();
                review = total - remembered;
            }
        }

        return FlashcardListDetailResponse.builder()
                .listId(list.getListId())
                .listName(list.getListName())
                .description(list.getDescription())
                .createdAt(list.getCreatedAt())
                .isPublic(list.getIsPublic())
                .isOwner(list.getUser().getUserId() == userId)
                .totalCards(total)
                .learnedCards(learned)
                .rememberedCards(remembered)
                .needReviewCards(review)
                .flashcards(cardItems)
                .build();
    }

    @Override
    public void addFlashcardToList(Long listId, FlashcardCreateRequest request, Long userId) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List không tồn tại"));

        if (!list.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền thêm từ vào danh sách này.");
        }

        Flashcard card = Flashcard.builder()
                .list(list)
                .frontText(request.getFrontText())
                .backText(request.getBackText())
                .category(request.getCategory())
                .createdAt(Instant.now())
                .build();

        flashcardRepository.save(card);
    }

    @Override
    public void updateFlashcardInList(Long listId, Long cardId, FlashcardCreateRequest request, Long userId) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List không tồn tại"));

        if (!list.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa danh sách này.");
        }

        Flashcard card = flashcardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Flashcard không tồn tại"));

        if (!card.getList().getListId().equals(listId)) {
            throw new AccessDeniedException("Flashcard không thuộc danh sách này.");
        }

        card.setFrontText(request.getFrontText());
        card.setBackText(request.getBackText());
        card.setCategory(request.getCategory());
        flashcardRepository.save(card);
    }

    @Override
    public void deleteFlashcard(Long listId, Long cardId, Long userId) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List không tồn tại"));

        if (!list.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa từ trong danh sách này.");
        }

        Flashcard card = flashcardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Từ không tồn tại"));

        if (!card.getList().getListId().equals(listId)) {
            throw new RuntimeException("Từ không thuộc danh sách.");
        }

        flashcardRepository.delete(card);
    }

    @Override
    public boolean togglePublicStatus(Long listId, Long userId) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List không tồn tại"));

        if (!list.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền sửa danh sách này.");
        }

        list.setIsPublic(!Boolean.TRUE.equals(list.getIsPublic()));
        flashcardListRepository.save(list);

        return list.getIsPublic();
    }

    @Override
    @Transactional
    public void updateFlashcardList(Long listId, Long userId, FlashcardListUpdateRequest request) {
        FlashcardList list = flashcardListRepository.findByListIdAndUser_UserId(listId, userId)
                .orElseThrow(() -> new RuntimeException("List not found or not yours"));

        list.setListName(request.getListName());
        list.setDescription(request.getDescription());

        // Xóa hết flashcards cũ
        flashcardRepository.deleteByList_ListId(listId);

        // Thêm lại flashcards mới
        List<Flashcard> newCards = request.getFlashcards().stream().map(f -> {
            Flashcard card = new Flashcard();
            card.setList(list);
            card.setFrontText(f.getFrontText());
            card.setBackText(f.getBackText());
            card.setCategory(f.getCategory());
            card.setCreatedAt(Instant.now());
            return card;
        }).collect(Collectors.toList());

        flashcardRepository.saveAll(newCards);
        flashcardListRepository.save(list);
    }
}

