package com.example.dice_talk.chatroom.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chatroom.dto.ChatRoomDto;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.mapper.ChatRoomMapper;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.response.ErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Tag(name = "ChatRoom API", description = "채팅방 관련 API")
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
//    @PostMapping
//    public ResponseEntity postChatRoom(@Valid @RequestBody ChatRoomDto.Post dto) {
//        ChatRoom chatRoom = mapper.chatRoomPostToChatRoom(dto);
//        ChatRoom created = chatRoomService.createChatRoom(chatRoom);
//        URI location = UriCreator.createUri(CHAT_ROOM_DEFAULT_URL, created.getChatRoomId());
//        return ResponseEntity.created(location).build();
//    }

    @Operation(summary = "채팅방 공지 수정", description = "특정 채팅방의 공지를 수정합니다.",
            // security = @SecurityRequirement(name = "JWT")
            responses = {
                    @ApiResponse(responseCode = "200", description = "공지 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    @PatchMapping("/{chat-room-id}")
    public ResponseEntity patchChatRoom(
            @PathVariable("chat-room-id") @Positive long chatRoomId,
            @Valid @RequestBody ChatRoomDto.Patch dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        dto.setChatRoomId(chatRoomId);
        ChatRoom chatRoom = chatRoomService.updateChatRoom(mapper.chatRoomPatchToChatRoom(dto));
        return new ResponseEntity(new SingleResponseDto<>(mapper.chatRoomToChatRoomSingleResponse(chatRoom)), HttpStatus.OK);
    }

    // 채팅방 전체조회 (관리자용)
    @Operation(summary = "전체 채팅방 조회", description = "전체 채팅방을 페이징 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity getChatRooms(@Positive @RequestParam int page,
                                       @Positive @RequestParam int size) {
        AuthorizationUtils.isAdmin();
        Page<ChatRoom> chatRoomPage = chatRoomService.findChatRooms(page, size);
        List<ChatRoom> chatRooms = chatRoomPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(
                mapper.chatRoomsToChatRoomMultiResponses(chatRooms), chatRoomPage
        ), HttpStatus.OK);
    }

    // 로그인한 사용자가 참가했던 1대1 채팅방 전체조회
    @Operation(summary = "내 채팅방 조회", description = "로그인한 사용자의 1:1 채팅방을 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))

            }
    )
    @GetMapping("/my-chat-room/{member-id}")
    public ResponseEntity getMyChatRooms(@PathVariable("member-id") @Positive long memberId,
                                         @Positive @RequestParam int page,
                                         @Positive @RequestParam int size,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Page<ChatRoom> chatRoomPage = chatRoomService.findMyCoupleChatRooms(page, size, memberId, customPrincipal.getMemberId());
        List<ChatRoom> chatRooms = chatRoomPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(
                mapper.chatRoomsToChatRoomMultiResponses(chatRooms), chatRoomPage
        ), HttpStatus.OK);
    }

    @Operation(summary = "채팅방 상세 조회", description = "특정 채팅방의 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/{chat-room-id}")
    public ResponseEntity getChatRoom(@PathVariable("chat-room-id") @Positive long chatRoomId,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(chatRoomId);
        return new ResponseEntity(new SingleResponseDto<>(
                mapper.chatRoomToChatRoomSingleResponse(chatRoom)
        ), HttpStatus.OK);
    }

    // 채팅방 강제종료(상태 비활성화로 변경) - 관리자용
    @Operation(summary = "채팅방 비활성화", description = "관리자가 채팅방을 비활성화 처리합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @DeleteMapping("/office/{chat-room-id}")
    public ResponseEntity deleteChatRoom(
            @PathVariable("chat-room-id") @Positive long chatRoomId
    ) {
        AuthorizationUtils.isAdmin();
        chatRoomService.deleteChatRoom(chatRoomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 특정 채팅방에 소속된 특정 회원의 상태를 '퇴장 상태' 로 변경하는 메서드
    @Operation(summary = "채팅방 퇴장 처리", description = "특정 사용자를 채팅방에서 퇴장 처리합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "퇴장 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("/{chat-room-id}/{member-id}")
    public ResponseEntity exitFromChatRoom(@PathVariable("chat-room-id") @Positive long chatRoomId,
                                           @PathVariable("chat-room-id") @Positive long memberId,
                                           @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        AuthorizationUtils.isAdminOrOwner(memberId, customPrincipal.getMemberId());
        chatRoomService.exitChatPart(chatRoomId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 대기열을 등록할 때 참여중인 채팅방이 있는지 확인하는 API 엔드포인트
    @Operation(summary = "참여 가능 여부 확인", description = "회원이 채팅방에 참여 가능한지 여부를 확인합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "확인 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/isPossible/{member-id}")
    public ResponseEntity verifyChatPart(@PathVariable("member-id") long memberId) {
        return new ResponseEntity<>(chatRoomService.isMemberPossibleToPart(memberId), HttpStatus.OK);
    }
}
