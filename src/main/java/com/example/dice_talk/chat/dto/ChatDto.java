package com.example.dice_talk.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/*
채팅 메세지 DTO
- 클라이언트와 서버 간 채팅 메세지를 주고받을 때 사용되는 DTO
- Post 클래스 : Client -> Server (메세지 전송 요청)
- Response 클래스 : Server -> Client (메세지 응답)
 */
public class ChatDto {


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Enter {
        private Long memberId;
        private String nickname;
        private Long chatRoomId;
    }

    /*
    Post 메시지 전송 요청
    - 클라이언트가 채팅 메세지를 보낼 때 사용하는 DTO
    - WebSocket 을 통해 '/pub/chat/{roomId}/sendMessage' 전송됨
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @NotBlank
        private String message;  //message 내용
        private String nickname;    //message 작성자 (webSocket 연결된 유저명)
        private long memberId;    //message 작성자의 고유 ID (DB 저장 위해 필요함)
        private long chatRoomId;    //message가 속한 채팅방 ID
    }

    /*
    Response(응답)
    - 서버가 클라이언트에게 메시지 전송 결과를 응답할 때 사용
    - 메세지가 정상적으로 저장된 후 WebSocket을 통해 구독 중인 클라이언트들에게 전송됨
    */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private long chatId;    //메세지의 고유 ID(DB에서 생성됨)
        private String message;     //메세지 내용
        private long memberId;      //메세지 작성자 (writer)
        private String nickName;    //메세지 작성자 ??
        private long chatRoomId;
        private LocalDateTime createdAt; //메시지 생성 시간 (서버에서 설정)
    }
}
