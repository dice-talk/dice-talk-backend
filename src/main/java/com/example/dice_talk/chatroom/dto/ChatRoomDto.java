package com.example.dice_talk.chatroom.dto;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
