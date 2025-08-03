package com.toeicify.toeic.repository.custom;

/**
 * Created by hungpham on 8/3/2025
 */
import com.toeicify.toeic.entity.QuestionGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface QuestionGroupRepositoryCustom {

    Optional<QuestionGroup> findByIdWithQuestionsAndOptionsQueryDsl(Long id);

    List<QuestionGroup> findByPartIdWithQuestionsAndOptionsQueryDsl(Long partId);

    Page<QuestionGroup> searchQuestionGroupsQueryDsl(Long partId, Pageable pageable);
}
