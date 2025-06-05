package com.example.dice_talk.question.repository;

import com.example.dice_talk.member.entity.QMember;
import com.example.dice_talk.question.entity.QQuestion;
import com.example.dice_talk.question.entity.Question;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.List;

public class QuestionRepositoryImpl implements QuestionRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public QuestionRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Question> searchGuestQuestions(Question.QuestionStatus status, String search, String searchType, Pageable pageable) {
        QQuestion question = QQuestion.question;
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        // 상태별 필터링
        if (status != null) {
            builder.and(question.questionStatus.eq(status));
        } else {
            builder.and(question.questionStatus.in(
                    Question.QuestionStatus.QUESTION_GUEST,
                    Question.QuestionStatus.QUESTION_GUEST_ANSWERED
            ));
        }

        // 검색어 처리
        if (search != null && !search.isEmpty()){
            switch (searchType){
                case "TITLE":
                    builder.and(question.title.containsIgnoreCase(search));
                    break;
                case "CONTENT":
                    builder.and(question.content.containsIgnoreCase(search));
                    break;
                case "AUTHOR":
                    builder.and(member.email.containsIgnoreCase(search));
                    break;
                case "TITLE_AUTHOR":
                    builder.and(
                            question.title.containsIgnoreCase(search)
                                    .or(member.email.containsIgnoreCase(search))
                    );
                    break;
                default:
                    builder.and(question.title.containsIgnoreCase(search));
            }
        }

        // 쿼리 생성
        JPAQuery<Question> query = queryFactory
                .selectFrom(question)
                .leftJoin(question.member, member)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 (최신순, 오래된 순)
        if (pageable.getSort().isSorted()){
            pageable.getSort().forEach(order -> {
                if ("questionId".equals(order.getProperty())){
                    query.orderBy(order.isAscending() ?
                            question.questionId.asc() :
                            question.questionId.desc());
                }
            });
        } else {
            // 기본 : 최신순
            query.orderBy(question.questionId.desc());
        }

        List<Question> content = query.fetch();
        Long total = queryFactory
                .selectFrom(question)
                .leftJoin(question.member, member)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
