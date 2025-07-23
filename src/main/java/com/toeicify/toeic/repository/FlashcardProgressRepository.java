package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.FlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Long> {
    Optional<FlashcardProgress> findByUser_UserIdAndList_ListId(Long userId, Long listId);
}

