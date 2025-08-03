package com.toeicify.toeic.repository;

import com.toeicify.toeic.projection.ExamCategoryWithCount;
import com.toeicify.toeic.entity.ExamCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query("SELECT ec as category, COUNT(e) as examCount " +
            "FROM ExamCategory ec LEFT JOIN Exam e ON ec.categoryId = e.examCategory.categoryId " +
            "GROUP BY ec " +
            "ORDER BY ec.categoryId")
    Page<ExamCategoryWithCount> findAllCategoriesWithExamCount(Pageable pageable);

    long countByCategoryId(Long id);

}
