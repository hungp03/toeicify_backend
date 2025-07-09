package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.ExamCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by hungpham on 7/10/2025
 */
@Repository
public interface ExamCategoryRepository extends JpaRepository<ExamCategory, Long> {
    Optional<ExamCategory> findByCategoryName(String categoryName);

    boolean existsByCategoryName(String categoryName);

    boolean existsByCategoryNameAndCategoryIdNot(String name, Long id);
}
