package com.example.dice_talk.member.repository;

import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberRepositoryCustom {
    Page<Member> searchMembers(
            String search,
            Member.MemberStatus memberStatus,
            Member.Gender gender,
            String ageGroup,
            Pageable pageable
    );

    Page<MemberDto.BannedMemberListResponse> searchBannedMembers(
            String search,
            Member.Gender gender,
            String ageGroup,
            String bannedAtStart,
            String bannedAtEnd,
            Pageable pageable
    );

    //웹페이지 : 금일 신규 가입자 수
    int countTotalSignups(LocalDateTime start, LocalDateTime end);
    //웹페이지 : 주간 신규 가입자 조회
    List<DailyCountDto> countSignupsByDate(LocalDateTime start, LocalDateTime end);
    //웹피이지 : 신규 가입자 이름 조회
    List<String> findNamesByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
