package com.example.dice_talk.roomevent.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.event.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomEventId;

    @Column(nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private Long senderId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    private RoomEventType roomEventType;

    // message 컬럼은 null 허용
    @Column(nullable = true)
    private String message;

    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
        if(!chatRoom.getRoomEvents().contains(this)){
            chatRoom.setRoomEvent(this);
        }
    }

    public enum RoomEventType {
        PICK("메세지 없이 상대방 선택"),
        PICK_MESSAGE("선택한 상대방에게 메세지 보내기");

        @Getter
        private String status;

        RoomEventType(String status) {
            this.status = status;
        }
    }
}
