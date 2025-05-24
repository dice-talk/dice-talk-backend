package com.example.dice_talk.roomevent.dto;

import com.example.dice_talk.roomevent.entity.RoomEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(description = "채팅방 이벤트 DTO")
public class RoomEventDto {

    @Schema(description = "이벤트 생성 요청 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post {
        @Schema(description = "수신자 회원 ID", example = "123")
        private Long receiverId;

        @Schema(description = "발신자 회원 ID", example = "456")
        private Long senderId;

        @Schema(description = "이벤트 유형 ID", example = "789")
        private Long eventId;

        @Schema(description = "채팅방 ID", example = "1011")
        private Long chatRoomId;

        @Schema(description = "이벤트 메시지 내용", example = "더 알아가고 싶어요")
        private String message;

        @Schema(description = "이벤트 타입", example = "PICK_MESSAGE")
        private RoomEvent.RoomEventType roomEventType;
    }

    @Schema(description = "이벤트 응답 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        @Schema(description = "채팅방 이벤트 ID", example = "1")
        private Long roomEventId;

        @Schema(description = "수신자 회원 ID", example = "123")
        private Long receiverId;

        @Schema(description = "발신자 회원 ID", example = "456")
        private Long senderId;

        @Schema(description = "채팅방 ID", example = "1011")
        private Long chatRoomId;

        @Schema(description = "이벤트 메시지 내용", example = "더 알아가고 싶어요")
        private String message;

        @Schema(description = "이벤트 타입", example = "PICK_MESSAGE")
        private RoomEvent.RoomEventType roomEventType;

        @Schema(description = "생성 일시", example = "2025-05-24T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정 일시", example = "2025-05-24T10:05:00")
        private LocalDateTime modifiedAt;
    }
}
