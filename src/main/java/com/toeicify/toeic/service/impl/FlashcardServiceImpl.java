package com.toeicify.toeic.service.impl;

import com.toeicify.toeic.dto.request.flashcard.CreateFlashcardListRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardCreateRequest;
import com.toeicify.toeic.dto.request.flashcard.FlashcardListUpdateRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListDetailResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListResponse;
import com.toeicify.toeic.entity.Flashcard;
import com.toeicify.toeic.entity.FlashcardList;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.exception.ResourceAlreadyExistsException;
import com.toeicify.toeic.exception.ResourceInvalidException;
import com.toeicify.toeic.exception.ResourceNotFoundException;
import com.toeicify.toeic.mapper.FlashcardMapper;
import com.toeicify.toeic.repository.FlashcardListRepository;
import com.toeicify.toeic.repository.FlashcardRepository;
import com.toeicify.toeic.service.FlashcardService;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardServiceImpl implements FlashcardService {
    private final FlashcardListRepository flashcardListRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserService userService;
    private final FlashcardMapper flashcardMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getFlashcardLists(String type, int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();

        int adjustedSize = switch (type) {
            case "mine" -> 7;
            case "explore" -> 8;
            default -> size;
        };
        Pageable pageable = PageRequest.of(page, adjustedSize, Sort.by("createdAt").descending());

        Page<FlashcardList> pageResult = getPageResultByType(type, userId, pageable);
        Page<FlashcardListResponse> dtoPage = mapToResponsePage(pageResult, type);

        return PaginationResponse.from(dtoPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse searchFlashcardLists(String keyword, int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId(); // Lấy user hiện tại
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<FlashcardList> pageResult = flashcardListRepository.searchPublicFlashcardsExcludingUserByListName(userId, keyword, pageable);

        Page<FlashcardListResponse> dtoPage = pageResult.map(flashcardMapper::toResponse);

        return PaginationResponse.from(dtoPage, pageable);
    }

    @Override
    public FlashcardListResponse createFlashcardList(CreateFlashcardListRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userService.findById(userId);

        if (flashcardListRepository.existsByListNameAndUser_UserId(request.listName(), userId)) {
            throw new ResourceAlreadyExistsException("List name already exists");
        }


        FlashcardList list = FlashcardList.builder()
                .listName(request.listName())
                .description(request.description())
                .user(user)
                .isPublic(false)
                .createdAt(Instant.now())
                .build();

        return flashcardMapper.toResponse(flashcardListRepository.save(list));
    }

    @Override
    public FlashcardListDetailResponse getFlashcardListDetail(Long listId) {
        Long userId = SecurityUtil.getCurrentUserId();
        FlashcardList list = findFlashcardListById(listId);

        List<FlashcardListDetailResponse.FlashcardItem> cardItems = list.getFlashcards().stream()
                .sorted(Comparator.comparing(Flashcard::getCardId))
                .map(flashcardMapper::toFlashcardItem)
                .toList();

        return FlashcardListDetailResponse.builder()
                .listId(list.getListId())
                .listName(list.getListName())
                .description(list.getDescription())
                .createdAt(list.getCreatedAt())
                .isPublic(list.getIsPublic())
                .isOwner(Objects.equals(list.getUser().getUserId(), userId))
                .inProgress(list.getInProgress())
                .flashcards(cardItems)
                .build();
    }

    @Override
    public PaginationResponse getPaginatedFlashcards(Long listId, int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        FlashcardList list = findFlashcardListById(listId);

        validateListAccess(list, userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("cardId").ascending());
        Page<Flashcard> pageResult = flashcardRepository.findByList_ListIdOrderByCardIdAsc(listId, pageable);
        Page<FlashcardListDetailResponse.FlashcardItem> mappedPage = pageResult.map(flashcardMapper::toFlashcardItem);

        return PaginationResponse.from(mappedPage, pageable);
    }

    @Override
    public void addFlashcardToList(Long listId, FlashcardCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        FlashcardList list = getOwnedList(listId, userId);

        validateFlashcardAddExists(listId,
                request.frontText().trim(),
                request.backText().trim(),
                request.category().trim());

        Flashcard card = buildFlashcard(list, request);
        flashcardRepository.save(card);
    }

    @Override
    public void updateFlashcardInList(Long listId, Long cardId, FlashcardCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        getOwnedList(listId, userId);
        Flashcard card = getCardInListOrThrow(cardId, listId);
        if (card.getFrontText().equals(request.frontText()) &&
                card.getBackText().equals(request.backText()) &&
                card.getCategory().equals(request.category())) {
            throw new ResourceInvalidException("No changes detected in the update request.");
        }
        validateFlashcardUpdateExists(listId,cardId,request);
        updateFlashcardFields(card, request);
        flashcardRepository.save(card);
    }

    @Override
    public void deleteFlashcard(Long listId, Long cardId) {
        Long userId = SecurityUtil.getCurrentUserId();
        getOwnedList(listId, userId);
        Flashcard card = getCardInListOrThrow(cardId, listId);

        flashcardRepository.delete(card);
    }

    @Override
    public boolean togglePublicStatus(Long listId) {
        Long userId = SecurityUtil.getCurrentUserId();
        FlashcardList list = getOwnedList(listId, userId);

        list.setIsPublic(!Boolean.TRUE.equals(list.getIsPublic()));
        flashcardListRepository.save(list);
        return list.getIsPublic();
    }

    @Override
    @Transactional
    public void updateFlashcardList(Long listId, FlashcardListUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        FlashcardList list = flashcardListRepository.findByListIdAndUser_UserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found or not yours"));

        if (!list.getListName().equals(request.listName())
                && flashcardListRepository.existsByListNameAndUser_UserId(request.listName(), userId)) {
            throw new ResourceAlreadyExistsException("List name already exists");
        }
        validateDuplicateFlashcards(request.flashcards());
        updateListBasicInfo(list, request);
        replaceFlashcards(list, request.flashcards());

        flashcardListRepository.save(list);
    }

    @Override
    public void markListInProgress(Long listId) {
        updateListProgressStatus(listId, true);
    }

    @Override
    public void stopLearningList(Long listId) {
        updateListProgressStatus(listId, false);
    }

    private Page<FlashcardList> getPageResultByType(String type, Long userId, Pageable pageable) {
        return switch (type) {
            case "mine" -> flashcardListRepository.findByUser_UserId(userId, pageable);
            case "learning" -> flashcardListRepository.findInProgressByUserId(userId, pageable);
            case "explore" -> flashcardListRepository.findPublicFlashcardsExcludingUser(userId, pageable);
            default -> throw new ResourceInvalidException("Invalid type param: " + type);
        };
    }

    private Page<FlashcardListResponse> mapToResponsePage(Page<FlashcardList> pageResult, String type) {
        return "explore".equals(type)
                ? pageResult.map(this::mapToExploreResponse)
                : pageResult.map(flashcardMapper::toResponse);
    }

    private FlashcardListResponse mapToExploreResponse(FlashcardList list) {
        FlashcardListResponse dto = flashcardMapper.toResponse(list);
        dto.setOwnerName(list.getUser().getFullName());
        return dto;
    }

    private FlashcardList findFlashcardListById(Long listId) {
        return flashcardListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found"));
    }

    private void validateFlashcardAddExists(Long listId, String frontText, String backText, String category){
        boolean exists = flashcardRepository.existsByList_ListIdAndFrontTextAndBackTextAndCategory(
                listId,
                frontText,
                backText,
                category
        );

        if (exists) {
            throw new ResourceAlreadyExistsException(
                    "Flashcard already exists in this list."
            );
        }
    }
    
    private void validateFlashcardUpdateExists(Long listId, Long cardId, FlashcardCreateRequest request){
        boolean exists = flashcardRepository.existsByList_ListIdAndFrontTextAndBackTextAndCategoryAndCardIdNot(
                listId,
                request.frontText().trim(),
                request.backText().trim(),
                request.category().trim(),
                cardId
        );

        if (exists) {
            throw new ResourceAlreadyExistsException(
                    "Another flashcard with the same front text, back text, and category already exists in this list."
            );
        }

    }

    private void validateDuplicateFlashcards(List<FlashcardCreateRequest> flashcards) {
        Set<String> uniqueCards = new HashSet<>();
        for (FlashcardCreateRequest card : flashcards) {
            String key = card.frontText().trim().toLowerCase() +
                    "|" + card.backText().trim().toLowerCase() +
                    "|" + card.category().trim().toLowerCase();
            if (!uniqueCards.add(key)) {
                throw new ResourceInvalidException("Duplicate flashcard detected: " + card.frontText());
            }
        }
    }

    private void validateListAccess(FlashcardList list, Long userId) {
        boolean isOwner = userId.equals(list.getUser().getUserId());
        boolean isPublic = Boolean.TRUE.equals(list.getIsPublic());

        if (!isOwner && !isPublic) {
            throw new AccessDeniedException("You do not have permission to access this flashcard list.");
        }
    }

    private FlashcardList getOwnedList(Long listId, Long userId) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard list not found"));

        if (!list.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this flashcard list.");
        }
        return list;
    }

    private Flashcard getCardInListOrThrow(Long cardId, Long listId) {
        Flashcard card = flashcardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));

        if (!card.getList().getListId().equals(listId)) {
            throw new ResourceInvalidException("The flashcard does not belong to this list.");
        }
        return card;
    }

    private Flashcard buildFlashcard(FlashcardList list, FlashcardCreateRequest request) {
        return Flashcard.builder()
                .list(list)
                .frontText(request.frontText())
                .backText(request.backText())
                .category(request.category())
                .createdAt(Instant.now())
                .build();
    }

    private void updateFlashcardFields(Flashcard card, FlashcardCreateRequest request) {
        card.setFrontText(request.frontText());
        card.setBackText(request.backText());
        card.setCategory(request.category());
    }

    private void updateListBasicInfo(FlashcardList list, FlashcardListUpdateRequest request) {
        list.setListName(request.listName());
        list.setDescription(request.description());
    }

    private void replaceFlashcards(FlashcardList list, List<FlashcardCreateRequest> flashcardRequests) {
        // Xóa hết flashcards cũ
        flashcardRepository.deleteByList_ListId(list.getListId());

        // Thêm lại flashcards mới
        List<Flashcard> newCards = flashcardRequests.stream()
                .map(request -> buildFlashcard(list, request))
                .collect(Collectors.toList());

        flashcardRepository.saveAll(newCards);
    }

    private void updateListProgressStatus(Long listId, boolean inProgress) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard list is not found"));

        list.setInProgress(inProgress);
        flashcardListRepository.save(list);
    }
    @Override
    @Transactional
    public void deleteFlashcardList(Long listId) {
        Long userId = SecurityUtil.getCurrentUserId();
        FlashcardList list = findFlashcardListById(listId);
        if (!Objects.equals(list.getUser().getUserId(), userId)) {
            throw new AccessDeniedException("You are not allowed to delete this list");
        }
        flashcardRepository.deleteByList_ListId(listId);
        flashcardListRepository.delete(list);
    }
}