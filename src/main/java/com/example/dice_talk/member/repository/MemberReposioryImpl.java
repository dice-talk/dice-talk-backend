package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import java.util.List;

public class MemberReposioryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public MemberReposioryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Member> searchMembers(String search, Member.MemberStatus memberStatus, Member.Gender gender, String ageGroup,Pageable pageable) {
        QMember member = QMember.member;
        BooleanBuilder builder = new BooleanBuilder();

        // 이름 + 이메일 통합검색
        if (search != null && !search.isEmpty()){
            builder.and(member.name.containsIgnoreCase(search)
                    .or(member.email.containsIgnoreCase(search)));
        }

        // 상태
        if (memberStatus != null) {
            builder.and(member.memberStatus.eq(memberStatus));
        }
        // 성별
        if (gender != null) {
            builder.and(member.gender.eq(gender));
        }
        // 연령대
        if (ageGroup != null && !ageGroup.isEmpty()) {
            builder.and(member.ageGroup.eq(ageGroup));
        }

        // Pageable의 Sort를 QueryDSL에 적용
        JPAQuery<Member> query = queryFactory
                .selectFrom(member)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // Sort 적용
        for (Sort.Order order : pageable.getSort()) {
            switch (order.getProperty()) {
                case "memberId":
                    query.orderBy(order.isAscending() ? member.memberId.asc() : member.memberId.desc());
                    break;
                case "name":
                    query.orderBy(order.isAscending() ? member.name.asc() : member.name.desc());
                    break;
                case "email":
                    query.orderBy(order.isAscending() ? member.email.asc() : member.email.desc());
                    break;
                case "ageGroup":
                    query.orderBy(order.isAscending() ? member.ageGroup.asc() : member.ageGroup.desc());
                    break;
                default:
                    query.orderBy(member.memberId.desc());
            }
        }

        List<Member> content = query.fetch();
        long total = queryFactory.selectFrom(member).where(builder).fetchCount();

        return new PageImpl<>(content, pageable, total);

    }

}
