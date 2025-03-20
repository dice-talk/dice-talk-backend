package com.example.dice_talk.notice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Enumerated(value = EnumType.STRING)
    private NoticeType noticeType = NoticeType.NOTICE;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private String image;

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Enumerated(value = EnumType.STRING)
    private NoticeStatus noticeStatus = NoticeStatus.ONGOING;

    @Column(nullable = false)
    private int noticeImportance;

    public enum NoticeStatus{
        ONGOING("진행중"),
        CLOSED("종료"),
        SCHEDULED("진행예정");

        @Getter
        private String status;

        NoticeStatus(String status) {
            this.status = status;
        }
    }

    public enum NoticeType{
        NOTICE("공지사항"),
        EVENT("이벤트");

        @Getter
        private String status;

        NoticeType(String status) {
            this.status = status;
        }
    }
}
