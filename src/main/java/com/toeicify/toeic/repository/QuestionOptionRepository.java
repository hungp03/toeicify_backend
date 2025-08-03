package com.toeicify.toeic.repository;

/**
 * Created by hungpham on 8/3/2025
 */
import com.toeicify.toeic.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionQuestionIdOrderByOptionLetter(Long questionId);

    void deleteByQuestionQuestionId(Long questionId);
}
