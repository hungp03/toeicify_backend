package com.toeicify.toeic.mapper;

import com.toeicify.toeic.dto.response.flashcard.FlashcardListDetailResponse;
import com.toeicify.toeic.dto.response.flashcard.FlashcardListResponse;
import com.toeicify.toeic.entity.Flashcard;
import com.toeicify.toeic.entity.FlashcardList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlashcardMapper {
    @Mapping(source = "list.listId", target = "listId")
    @Mapping(source = "list.listName", target = "listName")
    @Mapping(source = "list.description", target = "description")
    @Mapping(source = "list.createdAt", target = "createdAt")
    @Mapping(target = "cardCount", expression = "java(list.getFlashcards() != null ? list.getFlashcards().size() : 0)")
    FlashcardListResponse toResponse(FlashcardList list);

    FlashcardListDetailResponse.FlashcardItem toFlashcardItem(Flashcard card);
}

