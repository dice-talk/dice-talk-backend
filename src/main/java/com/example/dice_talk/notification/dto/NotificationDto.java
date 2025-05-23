package com.example.dice_talk.notification.dto;

import com.example.dice_talk.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class NotificationDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @NotBlank(message = "알림 내용은 필수입니다.")
        private String content;

        @NotNull(message = "수신자 ID는 필수입니다.")
        private Long receiverId;

        @NotNull(message = "알림 타입은 필수입니다.")
        private Notification.NotificationType type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private Long notificationId;

        private boolean isRead;

        private String content;

        private Long receiverId;

        private Notification.NotificationType type;

        private LocalDateTime createdAt;
    }
}
