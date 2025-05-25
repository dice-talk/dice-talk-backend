package com.example.dice_talk.chat.controller;

import com.example.dice_talk.chat.dto.ChatDto;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.mapper.ChatMapper;
import com.example.dice_talk.chat.service.ChatService;
import com.example.dice_talk.chatroom.config.SessionRegistry;
import com.example.dice_talk.chatroom.config.StompHandler;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

/**
 * ✅ **MessageController (채팅 메시지 컨트롤러)**
 * 🛠 역할:
 * - STOMP 메시지를 처리하는 **WebSocket 기반 메시지 컨트롤러**
 * - 메시지를 받아서 저장한 후 **구독 중인 클라이언트들에게 브로드캐스트**
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService; // 메시지 저장 서비스
    private final ChatRoomService chatRoomService; // 채팅방 서비스 (채팅방 검증 및 관리)
    private final ChatMapper mapper; // DTO <-> 엔티티 변환
    private final SimpMessagingTemplate messagingTemplate; //WebSocket을 통해 메시지를 전송하는 객체
    private final SessionRegistry sessionRegistry;  // 세션 정보 관리

    @MessageMapping("/chat/{roomId}/enter")
    public void enterChatRoomToMember(@DestinationVariable long roomId,
                              ChatDto.Enter enter,
                              SimpMessageHeaderAccessor headerAccessor) {
        // 세션 ID 가져오기
        String sessionId = headerAccessor.getSessionId();
        //URL로 받은 채팅방 ID 설정
        enter.setChatRoomId(roomId);

        //사용자 정보 설정
        Long memberId = enter.getMemberId();
//        String nickname = enter.getNickname();

        chatService.enterChatRoom(roomId, memberId, sessionId);

    }

    /**
     * ✅ sendMessage - 클라이언트가 보낸 메시지를 처리하는 메서드
     * - `/pub/chat/{roomId}/sendMessage` 엔드포인트로 메시지 전송 요청을 받음
     * - 클라이언트가 `roomId`에 해당하는 채팅방에 메시지를 보낼 때 사용됨
     *
     * @param roomId 채팅방 ID (STOMP `DestinationVariable`로 전달)
     * @param chatDto 클라이언트가 보낸 메시지 DTO
     * @param headerAccessor STOMP 헤더 정보 (세션 정보 포함)
     */

    // ✅ 클라이언트에서 이 경로로 메시지 발행
    // webSocketConfig /-> registry.enableSimpleBroker("/sub");
    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable long roomId,   //@PathVariable : 구독 및 발행 Url
                            ChatDto.Post chatDto,
                            SimpMessageHeaderAccessor headerAccessor) {
        /*SimpMessageHeaderAccessor
        - 메세지 헤더 관리 : 세션ID, 사용자 정보, 메시지 타입 등 중요한 메타데이터 관리
        - 세션정보 접근 : gerSessionId() 메소드를 통해 현재 WebSocket 세션의 고유 ID를 얻음, 세션 속성을 저장, 검색기능 제공
        - 메세지 타입 설정 : setMessageTypeIfNotSet() 메소드로 메세지 타입(Connect, Subscribe, Message 등) 지정
        -
         */

        // ✅ STOMP 세션 ID 가져오기
        String sessionId = headerAccessor.getSessionId();

        // ✅ 세션 ID를 기반으로 유저 정보 가져오기 (StompHandler에서 관리)
        String nickname = chatDto.getNickname();
        Long memberId = chatDto.getMemberId(); // ✅ memberId도 함께 가져오기

        // ✅ 메시지 작성자 정보 설정 (유저 이름 & 멤버 ID)
        chatDto.setNickname(nickname);
        chatDto.setMemberId(memberId);

        // ✅ 메시지를 생성하면서 chatRoom을 설정 (DB 저장 전)
        Chat chat = mapper.chatPostToChat(chatDto);

        // ✅ 메시지를 DB에 저장
        Chat savedChat = chatService.createChat(chat);

        // ✅ 저장된 메시지를 구독 중인 클라이언트들에게 전송
        ChatDto.Response responseChat = mapper.chatToChatResponse(savedChat);
        responseChat.setNickname(nickname); // 메시지 작성자 정보 추가

        // ✅ 해당 채팅방을 구독 중인 모든 클라이언트에게 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/" + roomId, responseChat);
        log.info("메시지 전송: {}", responseChat);
    }
}

