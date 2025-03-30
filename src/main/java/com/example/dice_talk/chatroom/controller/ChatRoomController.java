package com.example.dice_talk.chatroom.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chatroom.dto.ChatRoomDto;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.mapper.ChatRoomMapper;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chat-rooms")
@Validated
public class ChatRoomController {

    private final static String CHAT_ROOM_DEFAULT_URL = "/chatRooms";

    private final ChatRoomService chatRoomService;
    private final ChatRoomMapper mapper;

    public ChatRoomController(ChatRoomService chatRoomService, ChatRoomMapper mapper) {
        this.chatRoomService = chatRoomService;
        this.mapper = mapper;
    }

    // 채팅방 생성
    @PostMapping
    public ResponseEntity postChatRoom(@Valid @RequestBody ChatRoomDto.Post dto) {
        ChatRoom chatRoom = mapper.chatRoomPostToChatRoom(dto);
        ChatRoom created = chatRoomService.createChatRoom(chatRoom);
        URI location = UriCreator.createUri(CHAT_ROOM_DEFAULT_URL, created.getChatRoomId());
        return ResponseEntity.created(location).build();
    }

    // 공지 수정
    @PatchMapping("/{chat-room-id}")
    public ResponseEntity patchChatRoom(
            @PathVariable("chat-room-id") @Positive long chatRoomId,
            @Valid @RequestBody ChatRoomDto.Patch dto,
            @AuthenticationPrincipal CustomPrincipal customPrincipal){
        dto.setChatRoomId(chatRoomId);
        ChatRoom chatRoom = chatRoomService.updateChatRoom(mapper.chatRoomPatchToChatRoom(dto));
        return new ResponseEntity(new SingleResponseDto<>(mapper.chatRoomToChatRoomSingleResponse(chatRoom)), HttpStatus.OK);
    }

    // 채팅방 전체조회 (관리자용)
    @GetMapping
    public ResponseEntity getChatRooms(@Positive @RequestParam int page,
                                       @Positive @RequestParam int size,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Page<ChatRoom> chatRoomPage = chatRoomService.findChatRooms(page, size);
        List<ChatRoom> chatRooms = chatRoomPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(
                mapper.chatRoomsToChatRoomMultiResponses(chatRooms), chatRoomPage
        ), HttpStatus.OK);
    }

    // 로그인한 사용자가 참가했던 1대1 채팅방 전체조회
    @GetMapping("/my-chat-room/{member-id}")
    public ResponseEntity getMyChatRooms(@PathVariable("member-id") @Positive long memberId,
                                         @Positive @RequestParam int page,
                                         @Positive @RequestParam int size,
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Page<ChatRoom> chatRoomPage = chatRoomService.findMyCoupleChatRooms(page, size, memberId, customPrincipal.getMemberId());
        List<ChatRoom> chatRooms = chatRoomPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(
                mapper.chatRoomsToChatRoomMultiResponses(chatRooms), chatRoomPage
        ), HttpStatus.OK);
    }

    // 채팅방 상세 조회
    @GetMapping("/{chat-room-id}")
    public ResponseEntity getChatRoom(@PathVariable @Positive long chatRoomId,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(chatRoomId);
        return new ResponseEntity(new SingleResponseDto<>(
                mapper.chatRoomToChatRoomSingleResponse(chatRoom)
        ), HttpStatus.OK);
    }

    // 채팅방 삭제(상태 비활성화로 변경)
    @DeleteMapping("/{chat-room-id}")
    public ResponseEntity deleteChatRoom(
            @PathVariable("chat-room-id") @Positive long chatRoomId
    ){
        chatRoomService.deleteChatRoom(chatRoomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 특정 채팅방에 소속된 특정 회원의 상태를 '퇴장 상태' 로 변경하는 메서드
    @DeleteMapping("/{chat-room-id}/{member-id}")
    public ResponseEntity exitFromChatRoom(@PathVariable("chat-room-id") @Positive long chatRoomId,
                                           @PathVariable("chat-room-id") @Positive long memberId,
                                           @AuthenticationPrincipal CustomPrincipal customPrincipal){
        AuthorizationUtils.isAdminOrOwner(memberId, customPrincipal.getMemberId());
        chatRoomService.exitChatPart(chatRoomId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 대기열을 등록할 때 참여중인 채팅방이 있는지 확인하는 API 엔ㄷ포인트
    @GetMapping("/isPossible/{member-id}")
    public ResponseEntity verifyChatPart(@PathVariable("member-id") long memberId){
        return new ResponseEntity(chatRoomService.isMemberPossibleToPart(memberId), HttpStatus.OK);
    }
}
