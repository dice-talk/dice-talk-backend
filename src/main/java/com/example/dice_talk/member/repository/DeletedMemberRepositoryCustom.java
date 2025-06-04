package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeletedMemberRepositoryCustom {
    Page<MemberDto.DeletedMemberResponse> searchDeletedMembersWithMember(
            String search,
            Member.Gender gender,
            String ageGroup,
            String reason,
            String deletedAtStart,
            String deletedAtEnd,
            Pageable pageable
    );
}
