package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.DeletedMember;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.entity.QDeletedMember;
import com.example.dice_talk.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DeletedMemberRepositoryImpl implements DeletedMemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public DeletedMemberRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<MemberDto.DeletedMemberResponse> searchDeletedMembersWithMember(String search, Member.Gender gender, String ageGroup, String reason, String deletedAtStart, String deletedAtEnd, Pageable pageable) {
        QDeletedMember deletedMember = QDeletedMember.deletedMember;
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        // 이름 + 이메일 검색
        if (search != null && !search.isEmpty()) {
            builder.and(
                    member.name.containsIgnoreCase(search)
                            .or(member.email.containsIgnoreCase(search))
            );
        }

        // 성별 필터링
        if (gender != null) {
            builder.and(member.gender.eq(gender));
        }
        // 나이대 필터링
        if (ageGroup != null) {
            builder.and(member.ageGroup.eq(ageGroup));
        }
        // 사유 필터링
        if (reason != null && !reason.isEmpty()) {
            builder.and(deletedMember.reason.containsIgnoreCase(reason));
        }
        // 날짜범위 시작
        if (deletedAtStart != null && !deletedAtStart.isEmpty()) {
            builder.and(deletedMember.createdAt.goe(LocalDate.parse(deletedAtStart).atStartOfDay()));
        }
        // 날짜범위 종료
        if (deletedAtEnd != null && !deletedAtEnd.isEmpty()) {
            builder.and(deletedMember.createdAt.loe(LocalDate.parse(deletedAtEnd).atTime(23, 59, 59)));
        }

        // Join : DeletedMember.memberId = Member.memberId
        JPAQuery<Tuple> query = queryFactory
                .select(deletedMember, member)
                .from(deletedMember)
                .join(member).on(deletedMember.memberId.eq(member.memberId))
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬
        for (Sort.Order order : pageable.getSort()){
            if("deletedAt".equals(order.getProperty())){
                query.orderBy(order.isAscending() ? deletedMember.createdAt.asc()
                        : deletedMember.createdAt.desc());
            }
        }

        List<Tuple> tuples = query.fetch();
        List<MemberDto.DeletedMemberResponse> content = tuples.stream()
                .map(tuple -> {
                    DeletedMember del = tuple.get(deletedMember);
                    Member mem = tuple.get(member);
                    if(mem == null || del == null){
                        return null;
                    }
                    return MemberDto.DeletedMemberResponse.builder()
                            .memberId(mem.getMemberId())
                            .email(mem.getEmail())
                            .name(mem.getName())
                            .birth(mem.getBirth())
                            .gender(mem.getGender())
                            .region(mem.getRegion())
                            .ageGroup(mem.getAgeGroup())
                            .deleteReason(del.getReason())
                            .deletedAt(del.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());

        // total count
        Long total = queryFactory
                .select(deletedMember.count())
                .from(deletedMember)
                .join(member).on(deletedMember.memberId.eq(member.memberId))
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
