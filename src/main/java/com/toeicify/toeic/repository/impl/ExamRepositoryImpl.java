package com.toeicify.toeic.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toeicify.toeic.dto.response.exam.ExamListItemResponse;
import com.toeicify.toeic.entity.q.QExam;
import com.toeicify.toeic.entity.q.QExamCategory;
import com.toeicify.toeic.entity.q.QExamPart;
import com.toeicify.toeic.repository.custom.ExamRepositoryCustom;
import com.toeicify.toeic.util.enums.ExamStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExamRepositoryImpl implements ExamRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ExamListItemResponse> searchExams(String keyword, Long categoryId, Pageable pageable, boolean onlyPublic) {
        QExam exam = QExam.exam;
        QExamCategory category = QExamCategory.examCategory;
        QExamPart part = QExamPart.examPart;

        // Filter
        BooleanExpression keywordCondition = null;
        if (StringUtils.hasText(keyword)) {
            keywordCondition = exam.examName.containsIgnoreCase(keyword)
                    .or(exam.examDescription.containsIgnoreCase(keyword))
                    .or(category.categoryName.containsIgnoreCase(keyword));
        }

        BooleanExpression categoryCondition = categoryId != null ? category.categoryId.eq(categoryId) : null;
        BooleanExpression statusCondition = onlyPublic ? exam.status.eq(ExamStatus.PUBLIC) : null;

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
                        exam.createdAt,
                        exam.status.stringValue()
                ))
                .from(exam)
                .leftJoin(exam.examCategory, category)
                .where(keywordCondition, categoryCondition, statusCondition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(exam.createdAt.desc())
                .fetch();

        // Count
        Long total = queryFactory
                .select(exam.count())
                .from(exam)
                .leftJoin(exam.examCategory, category)
                .where(keywordCondition, categoryCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
