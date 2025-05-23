package com.example.dice_talk.chatroom.mapper;

import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chatroom.dto.ChatPartDto;
import com.example.dice_talk.chatroom.dto.ChatRoomDto;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.theme.entity.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatRoomMapper {
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

    @Mapping(target = "member.memberId", source = "memberId")
    ChatPart chatPartPostToChatPart(ChatPartDto.Post dto);

    ChatRoom chatRoomPatchToChatRoom(ChatRoomDto.Patch dto);

    @Mapping(target = "themeName", source = "theme.name")
    ChatRoomDto.SingleResponse chatRoomToChatRoomSingleResponse(ChatRoom chatRoom);

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


}
