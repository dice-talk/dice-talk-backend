package com.example.dice_talk.chat.entity;

import com.example.dice_talk.chattingroom.entity.ChattingRoom;
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
public class Chat {
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
    @JoinColumn(name = "member-id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "room-id")
    private ChattingRoom chattingRoom;
}
