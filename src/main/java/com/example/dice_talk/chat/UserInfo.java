package com.example.dice_talk.chat;

import lombok.Getter;

@Getter
public class UserInfo {
    private final Long memberId;
    private final String chatRoomId;


    public UserInfo(Long memberId, String chatRoomId) {
        this.memberId = memberId;
        this.chatRoomId = chatRoomId;
    }
}
