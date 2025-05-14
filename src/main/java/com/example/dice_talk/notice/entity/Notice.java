package com.example.dice_talk.notice.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notice extends BaseEntity {
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
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Enumerated(value = EnumType.STRING)
    private NoticeStatus noticeStatus = NoticeStatus.ONGOING;

    @Column(nullable = false)
    private int noticeImportance;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeImage> images = new ArrayList<>();

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

    public void setImage(NoticeImage noticeImage){
        if(noticeImage.getNotice() != this){
            noticeImage.setNotice(this);
        }
        if(!this.images.contains(noticeImage)){
            images.add(noticeImage);
        }
    }
}
