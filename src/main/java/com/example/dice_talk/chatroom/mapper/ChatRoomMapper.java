package com.example.dice_talk.chatroom.mapper;

import com.example.dice_talk.chat.dto.ChatDto;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.mapper.ChatMapper;
import com.example.dice_talk.chatroom.dto.ChatPartDto;
import com.example.dice_talk.chatroom.dto.ChatRoomDto;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.roomevent.mapper.RoomEventMapper;
import com.example.dice_talk.theme.entity.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ChatMapper.class, RoomEventMapper.class})
public interface ChatRoomMapper {
    ChatMapper chatMapper = Mappers.getMapper(ChatMapper.class);
    RoomEventMapper roomEventMapper = Mappers.getMapper(RoomEventMapper.class);
    // chatRoom Post
    @Mapping(target = "theme.themeId", source = "themeId")
    default ChatRoom chatRoomPostToChatRoom(ChatRoomDto.Post dto){
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomType(dto.getRoomType());
        Theme theme = new Theme();
        theme.setThemeId(dto.getThemeId());
        chatRoom.setTheme(theme);
        dto.getChatParts().forEach(chatPartPost ->{
            ChatPart chatPart = chatPartPostToChatPart(chatPartPost);
            chatPart.setChatRoom(chatRoom);
        });
        return chatRoom;
    }

    // ChatPart 생성
    @Mapping(target = "member.memberId", source = "memberId")
    ChatPart chatPartPostToChatPart(ChatPartDto.Post dto);

    // ChatRoom 수정
    ChatRoom chatRoomPatchToChatRoom(ChatRoomDto.Patch dto);

    // ChatRoom 상세조회
    @Mapping(target = "chatRoomId", source = "chatRoomId")
    @Mapping(target = "themeName", source = "theme.name")
    default ChatRoomDto.SingleResponse chatRoomToChatRoomSingleResponse(ChatRoom chatRoom, int page, int size) {
        ChatRoomDto.SingleResponse response = new ChatRoomDto.SingleResponse();
        response.setChatRoomId(chatRoom.getChatRoomId());
        response.setRoomType(chatRoom.getRoomType());
        response.setNotice(chatRoom.getNotice());
        response.setRoomStatus(chatRoom.getRoomStatus());
        response.setThemeName(chatRoom.getTheme().getName());
        response.setCreatedAt(chatRoom.getCreatedAt());
        response.setModifiedAt(chatRoom.getModifiedAt());

        // chats를 Page로 변환
        // chats를 Page로 변환 (페이지네이션 적용)
        List<ChatDto.Response> allChatResponses = chatMapper.chatsToChatResponses(chatRoom.getChats());
        int start = (page - 1) * size;
        int end = Math.min(start + size, allChatResponses.size());
        List<ChatDto.Response> pagedChatResponses = allChatResponses.subList(start, end);

        Page<ChatDto.Response> chatPage = new PageImpl<>(
                pagedChatResponses,
                PageRequest.of(page - 1, size, Sort.by("chatId").descending()),
                allChatResponses.size()
        );
        response.setChats(chatPage);

        // 나머지 필드 설정
        response.setChatParts(chatPartsToChatPartResponses(chatRoom.getChatParts()));
        response.setRoomEvents(roomEventMapper.roomEventsToResponses(chatRoom.getRoomEvents()));

        return response;
    }

    // ChatRoom 목록 조회
    default ChatRoomDto.MultiResponse chatRoomToChatRoomMultiResponse(ChatRoom chatRoom) {
        ChatRoomDto.MultiResponse dto = new ChatRoomDto.MultiResponse();
        dto.setChatRoomId(chatRoom.getChatRoomId());
        dto.setRoomStatus(chatRoom.getRoomStatus());
        List<Chat> chats = chatRoom.getChats();
        if (chats.isEmpty()) {
            dto.setLastChat("메세지가 존재하지 않습니다.");
        } else {
            dto.setLastChat(chats.get(chats.size() - 1).getMessage());
        }
        dto.setCreatedAt(chatRoom.getCreatedAt());
        dto.setModifiedAt(chatRoom.getModifiedAt());
        return dto;
    }

    List<ChatRoomDto.MultiResponse> chatRoomsToChatRoomMultiResponses(List<ChatRoom> chatRooms);

    // ChatPart 응답
    @Mapping(target = "memberId", source = "member.memberId")
    @Mapping(target = "chatRoomId", source = "chatRoom.chatRoomId")
    ChatPartDto.Response chatPartToChatPartResponse(ChatPart chatPart);

    List<ChatPartDto.Response> chatPartsToChatPartResponses(List<ChatPart> chatParts);
}
