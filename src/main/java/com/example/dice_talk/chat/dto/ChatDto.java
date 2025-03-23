package com.example.dice_talk.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

public class ChatDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @NotBlank
        private String message;

        @NotBlank
        private long memberId;

        @NotBlank
        private long chatRoomId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private long chatId;
        private String message;
        private long memberId;
        private String nickName;
        private long chatRoomId;
    }
}
