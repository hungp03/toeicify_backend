package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    void deleteByList_ListId(Long listId);

    Page<Flashcard> findByList_ListIdOrderByCardIdAsc(Long listId, Pageable pageable);

    boolean existsByList_ListIdAndFrontTextAndBackTextAndCategory(
            Long listId,
            String frontText,
            String backText,
            String category
    );
    boolean existsByList_ListIdAndFrontTextAndBackTextAndCategoryAndCardIdNot(
            Long listId,
            String frontText,
            String backText,
            String category,
            Long cardId
    );

}
