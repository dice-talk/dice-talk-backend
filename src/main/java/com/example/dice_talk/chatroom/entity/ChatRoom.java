package com.example.dice_talk.chatroom.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomId;

    @Enumerated(value = EnumType.STRING)
    private RoomType roomType = RoomType.GROUP;

    @Column
    private String notice;

    @Enumerated(value = EnumType.STRING)
    private RoomStatus roomStatus = RoomStatus.ROOM_ACTIVE;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.PERSIST)
    private List<Chat> chats = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.PERSIST)
    private List<ChatPart> chatParts = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.PERSIST)
    private List<RoomEvent> roomEvents = new ArrayList<>();

//    @Column
//    private String aiSuggestedTopic;
//
//    @Column
//    private LocalDateTime lastSuggestedTime;

    public void setChat(Chat chat){
        if(chat.getChatRoom() != this){
            chat.setChatRoom(this);
        }
        if(this.getChats().contains(this)){
            this.getChats().add(chat);
        }
    }

    public void setChatPart(ChatPart chatPart){
        if(chatPart.getChatRoom() != this){
            chatPart.setChatRoom(this);
        }
        if(!this.getChatParts().contains(chatPart)){
            this.getChatParts().add(chatPart);
        }
    }

    public void setRoomEvent(RoomEvent roomEvent){
        if(roomEvent.getChatRoom() != this){
            roomEvent.setChatRoom(this);
        }
        if(!this.getRoomEvents().contains(roomEvent)){
            this.getRoomEvents().add(roomEvent);
        }
    }

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
