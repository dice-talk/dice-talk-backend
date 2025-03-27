package com.example.dice_talk.chat.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
/*
Message 엔티티 (채팅메시지)
- 채팅 메시지를 데이터베이스에 저장하는 JPA 엔티티
- ChatRoom과 Member와 연결
 */
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

    @Column(nullable = false)
    private String nickname;

//    @Column(nullable = false)
//    private boolean isFlagged;
//
//    @Column
//    private String flaggedReason;

    /**
     * ✅ **작성자 (Member) 연관 관계 설정**
     * - `@ManyToOne(fetch = FetchType.LAZY)`: **N:1 관계 (여러 메시지가 하나의 유저에게 속함)**
     * - `@JoinColumn(name = "member_id", nullable = false)`: FK 설정 (외래키)
     * - `LAZY` 로딩: **필요할 때만 `Member` 정보를 가져와서 성능 최적화**
     */
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

//        /**
//     * ✅ **채팅방 (ChatRoom) 연관 관계 설정**
//     * - `@ManyToOne(fetch = FetchType.LAZY)`: **N:1 관계 (여러 메시지가 하나의 채팅방에 속함)**
//     * - `@JoinColumn(name = "chat_room_id", nullable = false)`: FK 설정 (외래키)
//     * - **지연 로딩 (`LAZY`)**을 사용하여 필요할 때만 `ChatRoom`을 로딩함 → **성능 최적화**
//     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "chat_room_id", nullable = false)
//    private ChatRoom chatRoom;

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
