package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.FlashcardList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FlashcardListRepository extends JpaRepository<FlashcardList, Long> {

    Page<FlashcardList> findByUser_UserId(Long userId, Pageable pageable);

    @Query("SELECT f FROM FlashcardList f WHERE f.user.userId = :userId AND f.inProgress = true")
    Page<FlashcardList> findInProgressByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT f FROM FlashcardList f WHERE f.isPublic = true AND f.user.userId <> :userId")
    Page<FlashcardList> findPublicFlashcardsExcludingUser(@Param("userId") Long userId, Pageable pageable);

    Optional<FlashcardList> findByListIdAndUser_UserId(Long listId, Long userId);

    boolean existsByListNameAndUser_UserId(String listName, Long userId);

    @Query("SELECT f FROM FlashcardList f " +
            "WHERE f.isPublic = true AND f.user.userId <> :userId " +
            "AND LOWER(f.listName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<FlashcardList> searchPublicFlashcardsExcludingUserByListName(@Param("userId") Long userId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);
}
