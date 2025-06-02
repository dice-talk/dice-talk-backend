package com.example.dice_talk.chatroom.dto;

import com.example.dice_talk.chatroom.entity.ChatPart;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "참가자 닉네임", example = "철수")
        private String nickname;

        @Schema(description = "회원 ID", example = "7")
        private long memberId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        @Schema(description = "참여 ID", example = "3")
        private long partId;

        @Schema(description = "참가자 닉네임", example = "영희")
        private String nickname;

        @Schema(description = "회원 ID", example = "5")
        private long memberId;

        @Schema(description = "채팅방 ID", example = "1")
        private long chatRoomId;

        @Schema(description = "참여 상태", example = "MEMBER_ENTER")
        private ChatPart.ExitStatus exitStatus;
    }

}
