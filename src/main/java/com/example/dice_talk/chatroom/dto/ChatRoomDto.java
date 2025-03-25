package com.example.dice_talk.chatroom.dto;

import com.example.dice_talk.chat.dto.ChatDto;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.roomevent.dto.RoomEventDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        private ChatRoom.RoomType roomType;
        private long themeId;
        List<ChatPartDto.Post> chatParts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patch{
        private String notice;
        private ChatRoom.RoomStatus roomStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingleResponse{
        private long chatRoomId;
        private ChatRoom.RoomType roomType;
        private String notice;
        private ChatRoom.RoomStatus roomStatus;
        private String themeName;
        private List<ChatDto.Response> chats;
        private List<ChatPartDto.Response> chatParts;
        // 수정해야함
        private List<RoomEventDto.Response> roomEvents;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiResponse{
        private long chatRoomId;
        private ChatRoom.RoomType roomType;
        private ChatRoom.RoomStatus roomStatus;
        private String lastChat;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
