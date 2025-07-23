package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    void deleteByList_ListId(Long listId);

}
