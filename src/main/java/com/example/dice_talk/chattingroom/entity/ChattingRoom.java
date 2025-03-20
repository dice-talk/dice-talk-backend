package com.example.dice_talk.chattingroom.entity;

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
public class ChattingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Enumerated(value = EnumType.STRING)
    private RoomType roomType = RoomType.GROUP;

    @Column(nullable = false)
    private String notice;

    @Enumerated(value = EnumType.STRING)
    private RoomStatus roomStatus = RoomStatus.ROOM_ACTIVE;

    @ManyToOne
    @JoinColumn(name = "theme-id")
    private Theme theme;

//    @Column
//    private String aiSuggestedTopic;
//
//    @Column
//    private LocalDateTime lastSuggestedTime;

    public enum RoomStatus{
        ROOM_ACTIVE("활성화"),
        ROOM_DEACTIVE("비활성화");

        @Getter
        private String status;

        RoomStatus(String status) {
            this.status = status;
        }
    }

    public enum RoomType{
        GROUP("단체 채팅방"),
        COUPLE("1:1 채팅방");

        @Getter
        private String status;

        RoomType(String status) {
            this.status = status;
        }
    }
}
