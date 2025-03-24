package com.example.dice_talk.member.mapper;

import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    Member memberPostToMember(MemberDto.Post post);

    Member memberPatchToMember(MemberDto.Patch patch);

    MemberDto.MyInfoResponse memberInfoToMemberInfoResponse(Member member);

    MemberDto.MyPageResponse memberToMemberResponse(Member member);

    List<Member> membersToMemberResponses(List<Member> members);

    //List<ChatPart> -> MyInfoResponse
    default MemberDto.MyPageResponse memberToMemberMyPageResponseDto(Member member) {
        MemberDto.MyPageResponse myPageResponse = new MemberDto.MyPageResponse();
        myPageResponse.setMemberId(member.getMemberId());

        if (member.getChatParts().isEmpty()) {
            myPageResponse.setExitStatus(MemberDto.MyPageResponse.RoomParticipation.ROOM_EXIT);
            myPageResponse.setNickname("뿌웅 날코");
            myPageResponse.setTotalDice(0);

            return myPageResponse;

        } else {
            ChatPart lastChat = member.getChatParts().get(member.getChatParts().size() - 1);
            //member의 ChatParts의 가장 마지막 chatPart에서 exitStatus가 Enter라면 채팅방에 참가중이고, 아니라면 참가중인 채팅방이 없다.
            MemberDto.MyPageResponse.RoomParticipation room =
                    lastChat.getExitStatus().equals(ChatPart.ExitStatus.MEMBER_ENTER) ?
                            MemberDto.MyPageResponse.RoomParticipation.ROOM_ENTER :
                            MemberDto.MyPageResponse.RoomParticipation.ROOM_EXIT;


            myPageResponse.setExitStatus(room);
            myPageResponse.setNickname(lastChat.getNickname());
            myPageResponse.setTotalDice(member.getTotalDice());

            return myPageResponse;
        }
    }


}
