package com.example.dice_talk.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

public class ChatPartDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @NotBlank
        private String nickname;
        @NotBlank
        private String profile;
        private long memberId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private long partId;
        private String nickname;
        private String profile;
        private long memberId;
        private long chatRoomId;
    }
}
