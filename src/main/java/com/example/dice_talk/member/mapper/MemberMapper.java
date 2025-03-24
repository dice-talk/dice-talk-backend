package com.example.dice_talk.member.mapper;

import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.Member;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    Member memberPostToMember(MemberDto.Post post);
    Member memberPatchToMember(MemberDto.Patch patch);
    MemberDto.MyInfoResponse memberInfoToMemberInfoResponse(Member member);
    MemberDto.MyPageResponse memberToMemberResponse(Member member);
    List<Member> membersToMemberResponses(List<Member> members);

//    default MemberDto.Response memberToMemberResponseDto(Member member){
//        MemberDto.Response dto = new MemberDto.Response();
//        dto.setMemberId(member.getMemberId());
//        dto.setMem
//m
//    }

}
