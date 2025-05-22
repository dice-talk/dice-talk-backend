package com.example.dice_talk.notification.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.websocket.server.ServerEndpoint;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification",
        indexes = {
                @Index(name = "idx_notification_member_read", columnList = "member_id, is_read")
        })
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false, name = "is_read")
    private boolean isRead;

    @Column(nullable = false, length = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(value = EnumType.STRING)
    private NotificationType type = NotificationType.NOTIFICATION;

    public enum NotificationType{
        NOTIFICATION("알림"),
        PAYMENT("다이스 충전"),
//        REPORT("신고"),
        QNA("QnA"),
        DICE_USAGE("다이스 사용"),
        NOTICE_EVENT("공지사항/이벤트"),
        CHAT_ROOM("채팅방");

        @Getter
        private String status;

        NotificationType(String status) {
            this.status = status;
        }
    }
}
