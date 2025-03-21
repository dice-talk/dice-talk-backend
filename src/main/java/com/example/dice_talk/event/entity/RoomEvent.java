package com.example.dice_talk.event.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.chatroom.entity.ChatRoom;
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
@DiscriminatorColumn(name = "DTYPE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class RoomEvent extends BaseEntity {
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

    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
        if(!chatRoom.getRoomEvents().contains(this)){
            chatRoom.setRoomEvent(this);
        }
    }
}
