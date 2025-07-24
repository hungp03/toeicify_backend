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
import com.toeicify.toeic.mapper.FlashcardListMapper;
import com.toeicify.toeic.repository.FlashcardListRepository;
import com.toeicify.toeic.repository.FlashcardRepository;
import com.toeicify.toeic.repository.UserRepository;
import com.toeicify.toeic.service.FlashcardListService;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashcardListServiceImpl implements FlashcardListService {

    private final FlashcardListRepository flashcardListRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final FlashcardListMapper flashcardListMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getFlashcardLists(String type, int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userService.findById(userId);
        if (type.equals("mine")) size = 7;
        else size = 8;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return switch (type) {
            case "mine" -> {
                Page<FlashcardList> pageResult = flashcardListRepository.findByUser_UserId(userId, pageable);
                Page<FlashcardListResponse> dtoPage = pageResult.map(flashcardListMapper::toResponse);
                yield PaginationResponse.from(dtoPage, pageable);
            }
            case "learning" -> {
                Page<FlashcardList> pageResult = flashcardListRepository.findInProgressByUserId(userId, pageable);
                Page<FlashcardListResponse> dtoPage = pageResult.map(flashcardListMapper::toResponse);
                yield PaginationResponse.from(dtoPage, pageable);
            }
            case "explore" -> {
                Page<FlashcardList> pageResult = flashcardListRepository.findPublicFlashcardsExcludingUser(userId, pageable);
                Page<FlashcardListResponse> dtoPage = pageResult.map(list -> {
                    FlashcardListResponse dto = flashcardListMapper.toResponse(list);
                    dto.setOwnerName(list.getUser().getFullName());
                    return dto;
                });
                yield PaginationResponse.from(dtoPage, pageable);
            }
            default -> throw new IllegalArgumentException("Invalid type param: " + type);
        };
    }

    @Override
    public FlashcardListResponse createFlashcardList(CreateFlashcardListRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userService.findById(userId);

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
    public FlashcardListDetailResponse getFlashcardListDetail(Long listId) {
        Long userId = SecurityUtil.getCurrentUserId();

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

        return FlashcardListDetailResponse.builder()
                .listId(list.getListId())
                .listName(list.getListName())
                .description(list.getDescription())
                .createdAt(list.getCreatedAt())
                .isPublic(list.getIsPublic())
                .isOwner(list.getUser().getUserId() == userId)
                .inProgress(list.getInProgress())
                .flashcards(cardItems)
                .build();
    }

    @Override
    public PaginationResponse getPaginatedFlashcards(Long listId, int page, int size) {
        Long userId = null;
        try {
            userId = SecurityUtil.getCurrentUserId();
        } catch (Exception e) {
            // Nếu không đăng nhập thì vẫn có thể xem nếu list là public
        }

        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));

        // Nếu user không phải chủ list và list không public → chặn truy cập
        if ((userId == null || !list.getUser().getUserId().equals(userId)) && !Boolean.TRUE.equals(list.getIsPublic())) {
            throw new RuntimeException("Bạn không có quyền truy cập danh sách này");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("cardId").ascending());
        Page<Flashcard> pageResult = flashcardRepository.findByList_ListIdOrderByCardIdAsc(listId, pageable);

        Page<FlashcardListDetailResponse.FlashcardItem> mappedPage = pageResult.map(card ->
                FlashcardListDetailResponse.FlashcardItem.builder()
                        .cardId(card.getCardId())
                        .frontText(card.getFrontText())
                        .backText(card.getBackText())
                        .category(card.getCategory())
                        .createdAt(card.getCreatedAt())
                        .build()
        );

        return PaginationResponse.from(mappedPage, pageable);
    }

    @Override
    public void addFlashcardToList(Long listId, FlashcardCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

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
    public void updateFlashcardInList(Long listId, Long cardId, FlashcardCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

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
    public void deleteFlashcard(Long listId, Long cardId) {
        Long userId = SecurityUtil.getCurrentUserId();

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
    public boolean togglePublicStatus(Long listId) {
        Long userId = SecurityUtil.getCurrentUserId();

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
    public void updateFlashcardList(Long listId, FlashcardListUpdateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        FlashcardList list = flashcardListRepository.findByListIdAndUser_UserId(listId,userId)
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
    @Override
    public void markListInProgress(Long listId) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List không tồn tại"));

        list.setInProgress(true);
        flashcardListRepository.save(list);
    }
    @Override
    public void stopLearningList(Long listId) {
        FlashcardList list = flashcardListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List không tồn tại"));
        list.setInProgress(false);
        flashcardListRepository.save(list);
    }

}

