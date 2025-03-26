package com.example.dice_talk.roomevent.dto;

import com.example.dice_talk.roomevent.entity.RoomEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class RoomEventDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post {
        private Long receiverId;

        private Long senderId;

        private Long eventId;

        private Long chatRoomId;

        private String message;

        private RoomEvent.RoomEventType roomEventType;
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
        private String message;
        private RoomEvent.RoomEventType roomEventType;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
