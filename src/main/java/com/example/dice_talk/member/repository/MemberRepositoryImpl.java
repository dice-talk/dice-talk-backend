package com.example.dice_talk.member.repository;

import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Member> searchMembers(String search, Member.MemberStatus memberStatus, Member.Gender gender, String ageGroup, Pageable pageable) {
        QMember member = QMember.member;
        BooleanBuilder builder = new BooleanBuilder();

        // 이름 + 이메일 통합검색
        if (search != null && !search.isEmpty()) {
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

    @Override
    public Page<MemberDto.BannedMemberListResponse> searchBannedMembers(String search, Member.Gender gender, String ageGroup, String bannedAtStart, String bannedAtEnd, Pageable pageable) {
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(member.memberStatus.eq(Member.MemberStatus.MEMBER_BANNED));

        // 이름+이메일 검색
        if (search != null && !search.isEmpty()) {
            builder.and(
                    member.name.containsIgnoreCase(search)
                            .or(member.email.containsIgnoreCase(search))
            );
        }
        if (gender != null) {
            builder.and(member.gender.eq(gender));
        }
        if (ageGroup != null && !ageGroup.isEmpty()) {
            builder.and(member.ageGroup.eq(ageGroup));
        }
        if (bannedAtStart != null && !bannedAtStart.isEmpty()) {
            builder.and(member.modifiedAt.goe(LocalDate.parse(bannedAtStart).atStartOfDay()));
        }
        if (bannedAtEnd != null && !bannedAtEnd.isEmpty()) {
            builder.and(member.modifiedAt.loe(LocalDate.parse(bannedAtEnd).atTime(23, 59, 59)));
        }

        // 정렬
        JPAQuery<Member> query = queryFactory
                .selectFrom(member)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        for (Sort.Order order : pageable.getSort()) {
            if ("bannedAt".equals(order.getProperty()) || "modifiedAt".equals(order.getProperty())) {
                query.orderBy(order.isAscending() ? member.modifiedAt.asc() : member.modifiedAt.desc());
            }
        }

        List<Member> content = query.fetch();
        Long total = queryFactory
                .selectFrom(member)
                .where(builder)
                .fetchCount();

        List<MemberDto.BannedMemberListResponse> responses = content.stream()
                .map(m -> MemberDto.BannedMemberListResponse.builder()
                        .memberId(m.getMemberId())
                        .email(m.getEmail())
                        .name(m.getName())
                        .birth(m.getBirth())
                        .region(m.getRegion())
                        .memberStatus(m.getMemberStatus())
                        .gender(m.getGender())
                        .ageGroup(m.getAgeGroup())
                        .bannedAt(m.getModifiedAt())
                        .build()).collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, total != null ? total : 0L);

    }

    //웹페이지 : 금일 신규 가입자 수
    @Override
    public int countTotalSignups(LocalDateTime start, LocalDateTime end) {
        QMember member = QMember.member;

        Long count = queryFactory
                .select(member.count())
                .from(member)
                .where(member.createdAt.between(start, end))
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }

    //웹페이지 : 주간 신규 가입자 수
    @Override
    public List<DailyCountDto> countSignupsByDate(LocalDateTime start, LocalDateTime end) {
        QMember member = QMember.member;


        DateExpression<LocalDate> dateOnly = Expressions.dateTemplate(
                LocalDate.class, "DATE({0})", member.createdAt);

        NumberExpression<Long> countExpr = member.count();

        return queryFactory
                .select(Projections.constructor(
                        DailyCountDto.class,
                        dateOnly,
                        countExpr.intValue()
                ))
                .from(member)
                .where(member.createdAt.between(start, end))
                .groupBy(dateOnly)
                .orderBy(dateOnly.asc())
                .fetch();

    }

    //웹페이지 : 신규 가입자 이름만 조회
    @Override
    public List<String> findNamesByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        QMember member = QMember.member;

        return queryFactory
                .select(member.name)
                .from(member)
                .where(member.createdAt.between(start, end))
                .fetch();
    }
}
