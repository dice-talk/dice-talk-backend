package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

}
