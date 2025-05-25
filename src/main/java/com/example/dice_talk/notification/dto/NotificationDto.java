package com.example.dice_talk.notification.dto;

import com.example.dice_talk.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class NotificationDto {
    @Schema(name = "NotificationPostDto", description = "알림 생성 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @Schema(description = "알림 내용", example = "새로운 메시지가 도착했습니다.")
        @NotBlank(message = "알림 내용은 필수입니다.")
        private String content;

        @Schema(description = "수신자 회원 ID", example = "3")
        @NotNull(message = "수신자 ID는 필수입니다.")
        private Long receiverId;

        @Schema(description = "알림 타입", example = "MESSAGE")
        @NotNull(message = "알림 타입은 필수입니다.")
        private Notification.NotificationType type;
    }

    @Schema(name = "NotificationResponseDto", description = "알림 응답 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        @Schema(description = "알림 ID", example = "10")
        private Long notificationId;

        @Schema(description = "읽음 여부", example = "false")
        private boolean isRead;

        @Schema(description = "알림 내용", example = "새로운 메시지가 도착했습니다.")
        private String content;

        @Schema(description = "수신자 회원 ID", example = "3")
        private Long receiverId;

        @Schema(description = "알림 타입", example = "MESSAGE")
        private Notification.NotificationType type;

        @Schema(description = "생성 시간", example = "2025-05-24T12:34:56")
        private LocalDateTime createdAt;
    }
}
