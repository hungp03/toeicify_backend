package com.toeicify.toeic.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toeicify.toeic.dto.response.exam.ExamListItemResponse;
import com.toeicify.toeic.entity.q.QExam;
import com.toeicify.toeic.entity.q.QExamCategory;
import com.toeicify.toeic.entity.q.QExamPart;
import com.toeicify.toeic.repository.custom.ExamRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by hungpham on 7/20/2025
 */
@Repository
@RequiredArgsConstructor
public class ExamRepositoryImpl implements ExamRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ExamListItemResponse> searchExams(String keyword, Long categoryId, Pageable pageable) {
        QExam exam = QExam.exam;
        QExamCategory category = QExamCategory.examCategory;
        QExamPart part = QExamPart.examPart;

        List<ExamListItemResponse> content = queryFactory
                .select(Projections.constructor(ExamListItemResponse.class,
                        exam.examId,
                        exam.examName,
                        exam.examDescription,
                        exam.totalQuestions,
                        category.categoryName,
                        // Subquery count parts
                        JPAExpressions.select(part.count().intValue())
                                .from(part)
                                .where(part.exam.eq(exam)),
                        exam.status.stringValue()
                ))
                .from(exam)
                .leftJoin(exam.examCategory, category)
                .where(
                        keyword != null && !keyword.isBlank() ? exam.examName.containsIgnoreCase(keyword) : null,
                        categoryId != null ? category.categoryId.eq(categoryId) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QExam.exam.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(exam.count())
                .from(exam)
                .leftJoin(exam.examCategory, category)
                .where(
                        keyword != null && !keyword.isBlank() ? exam.examName.containsIgnoreCase(keyword) : null,
                        categoryId != null ? category.categoryId.eq(categoryId) : null
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}

