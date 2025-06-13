package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRoomRepositoryCustom {
    Page<ChatRoom> searchChatRooms(
            Long themeId,
            ChatRoom.RoomStatus roomStatus,
            ChatRoom.RoomType roomType,
            Long chatRoomId,
            String createdAtStart,
            String createdAtEnd,
            Pageable pageable
    );
}
