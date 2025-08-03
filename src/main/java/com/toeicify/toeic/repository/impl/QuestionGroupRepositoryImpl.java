package com.toeicify.toeic.repository.impl;

/**
 * Created by hungpham on 8/3/2025
 */
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toeicify.toeic.entity.QuestionGroup;
import com.toeicify.toeic.entity.Question;
import com.toeicify.toeic.entity.QuestionOption;
import com.toeicify.toeic.repository.custom.QuestionGroupRepositoryCustom;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.toeicify.toeic.entity.q.QExamPart.examPart;
import static com.toeicify.toeic.entity.q.QQuestion.question;
import static com.toeicify.toeic.entity.q.QQuestionGroup.questionGroup;
import static com.toeicify.toeic.entity.q.QQuestionOption.questionOption;

@Repository
@RequiredArgsConstructor
public class QuestionGroupRepositoryImpl implements QuestionGroupRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Optional<QuestionGroup> findByIdWithQuestionsAndOptionsQueryDsl(Long id) {
        // Method 1: Using transform() to group results properly
        Map<QuestionGroup, List<Question>> result = queryFactory
                .selectFrom(questionGroup)
                .leftJoin(questionGroup.questions, question).fetchJoin()
                .leftJoin(question.options, questionOption).fetchJoin()
                .leftJoin(questionGroup.part, examPart).fetchJoin()
                .where(questionGroup.groupId.eq(id))
                .transform(GroupBy.groupBy(questionGroup).as(GroupBy.list(question)));

        if (result.isEmpty()) {
            return Optional.empty();
        }

        QuestionGroup group = result.keySet().iterator().next();
        List<Question> questions = result.get(group);

        // Set questions to the group
        group.setQuestions(questions);

        return Optional.of(group);
    }

    @Override
    public List<QuestionGroup> findByPartIdWithQuestionsAndOptionsQueryDsl(Long partId) {
        // Method 2: Fetch in stages to avoid MultipleBagFetchException

        // Step 1: Get QuestionGroups with Part info
        List<QuestionGroup> groups = queryFactory
                .selectFrom(questionGroup)
                .leftJoin(questionGroup.part, examPart).fetchJoin()
                .where(questionGroup.part.partId.eq(partId))
                .orderBy(questionGroup.groupId.asc())
                .fetch();

        if (groups.isEmpty()) {
            return groups;
        }

        // Step 2: Get Questions with Options using transform
        List<Long> groupIds = groups.stream().map(QuestionGroup::getGroupId).toList();

        Map<Long, List<Question>> questionsByGroupId = queryFactory
                .selectFrom(question)
                .leftJoin(question.options, questionOption).fetchJoin()
                .where(question.group.groupId.in(groupIds))
                .orderBy(question.questionId.asc())
                .transform(GroupBy.groupBy(question.group.groupId).as(GroupBy.list(question)));

        // Step 3: Set questions to groups
        groups.forEach(group -> {
            List<Question> questionsForGroup = questionsByGroupId.get(group.getGroupId());
            group.setQuestions(questionsForGroup != null ? questionsForGroup : List.of());
        });

        return groups;
    }

    @Override
    public Page<QuestionGroup> searchQuestionGroupsQueryDsl(Long partId, Pageable pageable) {
        // Base query
        JPAQuery<QuestionGroup> query = queryFactory
                .selectFrom(questionGroup)
                .leftJoin(questionGroup.part, examPart).fetchJoin();

        // Apply filters
        if (partId != null) {
            query.where(questionGroup.part.partId.eq(partId));
        }

        // Count query for pagination
        long total = query.fetchCount();

        // Fetch results with pagination
        List<QuestionGroup> groups = query
                .orderBy(questionGroup.groupId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Fetch questions and options for the page results
        if (!groups.isEmpty()) {
            List<Long> groupIds = groups.stream().map(QuestionGroup::getGroupId).toList();

            Map<Long, List<Question>> questionsByGroupId = queryFactory
                    .selectFrom(question)
                    .leftJoin(question.options, questionOption).fetchJoin()
                    .where(question.group.groupId.in(groupIds))
                    .orderBy(question.questionId.asc())
                    .transform(GroupBy.groupBy(question.group.groupId).as(GroupBy.list(question)));

            groups.forEach(group -> {
                List<Question> questionsForGroup = questionsByGroupId.get(group.getGroupId());
                group.setQuestions(questionsForGroup != null ? questionsForGroup : List.of());
            });
        }

        return new PageImpl<>(groups, pageable, total);
    }

    // Alternative method using subqueries for complex scenarios
    public Optional<QuestionGroup> findByIdWithQuestionsAndOptionsSubquery(Long id) {
        QuestionGroup group = queryFactory
                .selectFrom(questionGroup)
                .leftJoin(questionGroup.part, examPart).fetchJoin()
                .where(questionGroup.groupId.eq(id))
                .fetchOne();

        if (group == null) {
            return Optional.empty();
        }

        // Fetch questions separately
        List<Question> questions = queryFactory
                .selectFrom(question)
                .where(question.group.groupId.eq(id))
                .orderBy(question.questionId.asc())
                .fetch();

        // Fetch options for all questions
        if (!questions.isEmpty()) {
            List<Long> questionIds = questions.stream().map(Question::getQuestionId).toList();

            Map<Long, List<QuestionOption>> optionsByQuestionId = queryFactory
                    .selectFrom(questionOption)
                    .where(questionOption.question.questionId.in(questionIds))
                    .orderBy(questionOption.optionLetter.asc())
                    .transform(GroupBy.groupBy(questionOption.question.questionId).as(GroupBy.list(questionOption)));

            // Set options to questions
            questions.forEach(q -> {
                List<QuestionOption> options = optionsByQuestionId.get(q.getQuestionId());
                q.setOptions(options != null ? options : List.of());
            });
        }

        group.setQuestions(questions);
        return Optional.of(group);
    }

    // Batch fetch method for better performance
    public List<QuestionGroup> findByPartIdWithQuestionsAndOptionsBatch(Long partId) {
        // Step 1: Get all QuestionGroups
        List<QuestionGroup> groups = queryFactory
                .selectFrom(questionGroup)
                .leftJoin(questionGroup.part, examPart).fetchJoin()
                .where(questionGroup.part.partId.eq(partId))
                .orderBy(questionGroup.groupId.asc())
                .fetch();

        if (groups.isEmpty()) {
            return groups;
        }

        List<Long> groupIds = groups.stream().map(QuestionGroup::getGroupId).toList();

        // Step 2: Batch fetch all questions
        List<Question> allQuestions = queryFactory
                .selectFrom(question)
                .where(question.group.groupId.in(groupIds))
                .orderBy(question.questionId.asc())
                .fetch();

        if (!allQuestions.isEmpty()) {
            List<Long> questionIds = allQuestions.stream().map(Question::getQuestionId).toList();

            // Step 3: Batch fetch all options
            Map<Long, List<QuestionOption>> optionsByQuestionId = queryFactory
                    .selectFrom(questionOption)
                    .where(questionOption.question.questionId.in(questionIds))
                    .orderBy(questionOption.optionLetter.asc())
                    .transform(GroupBy.groupBy(questionOption.question.questionId).as(GroupBy.list(questionOption)));

            // Set options to questions
            allQuestions.forEach(q -> {
                List<QuestionOption> options = optionsByQuestionId.get(q.getQuestionId());
                q.setOptions(options != null ? options : List.of());
            });

            // Group questions by group ID
            Map<Long, List<Question>> questionsByGroupId = allQuestions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(q -> q.getGroup().getGroupId()));

            // Set questions to groups
            groups.forEach(group -> {
                List<Question> questionsForGroup = questionsByGroupId.get(group.getGroupId());
                group.setQuestions(questionsForGroup != null ? questionsForGroup : List.of());
            });
        }

        return groups;
    }
}
