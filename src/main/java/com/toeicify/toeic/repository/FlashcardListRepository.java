package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.FlashcardList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FlashcardListRepository extends JpaRepository<FlashcardList, Long> {

    List<FlashcardList> findByUser_UserId(Long userId);

    @Query("SELECT f.list FROM FlashcardProgress f WHERE f.user.userId = :userId")
    List<FlashcardList> findLearningByUserId(Long userId);

    @Query("SELECT f FROM FlashcardList f WHERE f.isPublic = true AND f.user.userId <> :userId")
    List<FlashcardList> findPublicFlashcardsExcludingUser(Long userId);

    Optional<FlashcardList> findByListIdAndUser_UserId(Long listId, Long userId);
}
