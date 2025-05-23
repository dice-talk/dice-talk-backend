package com.example.dice_talk.chat.mapper;

import com.example.dice_talk.chat.dto.ChatDto;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.member.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "chatRoom.chatRoomId", source = "chatRoomId")
    Chat chatEnterToChat(ChatDto.Enter enter);

    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "chatRoom.chatRoomId", source = "chatRoomId")
    Chat chatPostToChat(ChatDto.Post post);

    @Mapping(target = "memberId", source = "member.memberId")
    @Mapping(target = "chatRoomId", source = "chatRoom.chatRoomId")
    ChatDto.Response chatToChatResponse(Chat chat);

    List<ChatDto.Response> chatsToChatResponses(List<Chat> chats);
}
