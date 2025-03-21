package com.example.dice_talk.chat.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.member.entity.Member;
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
public class Chat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @Column(nullable = false)
    private String message;

//    @Column(nullable = false)
//    private boolean isFlagged;
//
//    @Column
//    private String flaggedReason;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
        if(!chatRoom.getChats().contains(this)){
            chatRoom.setChat(this);
        }
    }
}
