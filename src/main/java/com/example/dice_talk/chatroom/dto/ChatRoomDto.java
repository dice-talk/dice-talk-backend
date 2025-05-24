package com.example.dice_talk.chatroom.dto;

import com.example.dice_talk.chat.dto.ChatDto;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.roomevent.dto.RoomEventDto;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "채팅방 유형 (GROUP: 단체, COUPLE: 1:1)", example = "GROUP")
        private ChatRoom.RoomType roomType;

        @Schema(description = "연결된 테마 ID", example = "101")
        private long themeId;

        @Schema(description = "채팅방 참가자 목록")
        private List<ChatPartDto.Post> chatParts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patch{
        @Schema(description = "채팅방 ID", example = "1")
        private Long chatRoomId;

        @Schema(description = "채팅방 공지 내용", example = "공지사항입니다.")
        private String notice;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingleResponse{
        @Schema(description = "채팅방 ID", example = "1")
        private long chatRoomId;

        @Schema(description = "채팅방 유형", example = "COUPLE")
        private ChatRoom.RoomType roomType;

        @Schema(description = "채팅방 공지", example = "공지입니다.")
        private String notice;

        @Schema(description = "채팅방 상태", example = "ROOM_ACTIVE")
        private ChatRoom.RoomStatus roomStatus;

        @Schema(description = "테마 이름", example = "일상 토크")
        private String themeName;

        private List<ChatDto.Response> chats;
        private List<ChatPartDto.Response> chatParts;
        private List<RoomEventDto.Response> roomEvents;

        @Schema(description = "생성일")
        private LocalDateTime createdAt;

        @Schema(description = "수정일")
        private LocalDateTime modifiedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiResponse{
        @Schema(description = "채팅방 ID", example = "1")
        private long chatRoomId;

        @Schema(description = "채팅방 유형", example = "COUPLE")
        private ChatRoom.RoomType roomType;

        @Schema(description = "채팅방 상태", example = "ROOM_DEACTIVE")
        private ChatRoom.RoomStatus roomStatus;

        @Schema(description = "최근 채팅 내용", example = "안녕하세요!")
        private String lastChat;

        @Schema(description = "생성일")
        private LocalDateTime createdAt;

        @Schema(description = "수정일")
        private LocalDateTime modifiedAt;
    }
}
