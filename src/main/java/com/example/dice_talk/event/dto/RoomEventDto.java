package com.example.dice_talk.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class RoomEventDto {

    // 이벤트 타입은 자식 클래스에서 구현
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post {
        @NotNull
        private Long receiverId;

        @NotNull
        private Long senderId;

        @NotNull
        private Long chatRoomId;

        private Long eventId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long roomEventId;
        private Long receiverId;
        private Long senderId;
        private Long chatRoomId;
        private String eventType;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
