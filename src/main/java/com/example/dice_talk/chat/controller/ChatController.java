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
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


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

    // 클라이언트에서 이 경로로 메시지 발행
    // webSocketConfig /-> registry.enableSimpleBroker("/sub");
    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable long roomId,   //@PathVariable : 구독 및 발행 Url
                            ChatDto.Post chatDto,
                            SimpMessageHeaderAccessor headerAccessor) {
        // STOMP 세션 ID 가져오기
        String sessionId = headerAccessor.getSessionId();

        // 세션 ID를 기반으로 유저 정보 가져오기 (StompHandler에서 관리)
        String nickname = chatDto.getNickname();
        Long memberId = chatDto.getMemberId(); // ✅ memberId도 함께 가져오기

        // 메시지 작성자 정보 설정 (유저 이름 & 멤버 ID)
        chatDto.setNickname(nickname);
        chatDto.setMemberId(memberId);

        // 메시지를 생성하면서 chatRoom을 설정 (DB 저장 전)
        Chat chat = mapper.chatPostToChat(chatDto);

        // 메시지를 DB에 저장
        Chat savedChat = chatService.createChat(chat);

        // 저장된 메시지를 구독 중인 클라이언트들에게 전송
        ChatDto.Response responseChat = mapper.chatToChatResponse(savedChat);
        responseChat.setNickname(nickname); // 메시지 작성자 정보 추가

        // 해당 채팅방을 구독 중인 모든 클라이언트에게 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/" + roomId, responseChat);
        log.info("메시지 전송: {}", responseChat);
    }

    // --- 테스트용 임시 HTTP 엔드포인트 ---
    @PostMapping("/http/chat/{roomId}/sendMessage") // WebSocket 경로와 다르게 설정
    public ResponseEntity<ChatDto.Response> sendMessageViaHttp(
            @PathVariable long roomId,
            @RequestBody ChatDto.Post chatDto) { // HTTP 요청 본문으로 ChatDto.Post를 받음

        // chatDto에 chatRoomId 설정 (경로 변수로부터)
        // ChatDto.Post에 chatRoomId 필드가 없다면, Chat 객체 생성 시 수동으로 설정 필요
        // chatDto.setChatRoomId(roomId); // ChatDto.Post에 setter가 있다면 사용

        Chat chat = mapper.chatPostToChat(chatDto);
        // Chat 엔티티 생성 시 chatRoomId를 설정해야 함.
        // 만약 chatPostToChat 매퍼가 chatDto의 chatRoomId를 사용하지 않는다면,
        // ChatRoom 객체를 조회해서 chat 엔티티에 직접 설정해야 합니다.
        // 예: ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(roomId); // ChatRoomService에 해당 메소드가 있다고 가정
        //     chat.setChatRoom(chatRoom);
        //     chat.setChatRoomId(roomId) // Chat 엔티티에 직접 ID를 저장하는 필드가 있다면

        // ChatDto.Post에 chatRoomId 필드가 있고, mapper.chatPostToChat에서 이를 사용한다면 위 주석은 불필요.
        // 하지만 chatDto에 memberId와 nickname도 올바르게 채워져서 와야 합니다.

        // 가장 중요한 것은 ChatService.createChat(chat)이 호출될 때,
        // Chat 객체 내에 member (발신자) 정보와 chatRoom 정보가 올바르게 설정되어 있어야
        // 푸시 알림 수신자 결정 및 알림 내용 구성이 제대로 됩니다.

        Chat savedChat = chatService.createChat(chat); // 이 호출을 통해 푸시 알림 로직 실행
        ChatDto.Response responseChat = mapper.chatToChatResponse(savedChat);
        // responseChat.setNickname(chatDto.getNickname()); // Chat 엔티티에 nickname이 있으므로 savedChat.getNickname() 사용

        log.info("HTTP 테스트 - 메시지 생성됨 (푸시 알림 시도됨): {}", responseChat);
        return ResponseEntity.ok(responseChat);
    }
    // --- 테스트용 임시 HTTP 엔드포인트 끝 ---
}

