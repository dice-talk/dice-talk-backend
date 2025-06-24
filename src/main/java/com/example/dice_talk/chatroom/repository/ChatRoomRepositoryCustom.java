package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.dashboard.dto.DailyCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

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

    //웹페이지 : 주간 진행중인 채팅방 수
    List<DailyCountDto> countActiveRoomsByDate(
            LocalDateTime start,
            LocalDateTime end
    );

    //웹페이지 : 금일 진행중인 채팅방 수
    int countActiveRoomTotal(LocalDateTime start, LocalDateTime end);
}
