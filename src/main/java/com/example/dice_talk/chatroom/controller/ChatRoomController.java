package com.example.dice_talk.chatroom.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chatroom.dto.ChatRoomDto;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.mapper.ChatRoomMapper;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@SecurityRequirement(name = "JWT")
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

//    @Operation(summary = "채팅방 공지 수정", description = "관리자가 기존에 등록된 채팅방의 공지를 수정합니다.",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "채팅방 수정 성공",
//                            content = @Content(schema = @Schema(implementation = ChatRoomDto.SingleResponse.class))),
//                    @ApiResponse(responseCode = "403", description = "수정 권한 없음",
//                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
//                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),
//                    @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방 수정 요청",
//                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
//                                    examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"The requested resource could not be found.\"}")))}
//    )
//    @PatchMapping("/{chat-room-id}")
//    public ResponseEntity<SingleResponseDto<String>> patchChatRoom(@Parameter(description = "수정할 채팅방의 ID", example = "12")
//                                                                   @PathVariable("chat-room-id") @Positive long chatRoomId,
//                                                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                                                                           description = "채팅방 수정 요청 본문", required = true,
//                                                                           content = @Content(schema = @Schema(implementation = ChatRoomDto.Patch.class)))
//                                                                   @Valid @RequestBody ChatRoomDto.Patch dto,
//                                                                   @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
//        dto.setChatRoomId(chatRoomId);
//        ChatRoom chatRoom = chatRoomService.updateChatRoom(mapper.chatRoomPatchToChatRoom(dto));
//        return new ResponseEntity(new SingleResponseDto<>(chatRoom.getNotice()), HttpStatus.OK);
//    }

    // 채팅방 전체조회 (관리자용)
    @Operation(summary = "전체 채팅방 목록 조회", description = "관리자가 전체 채팅방 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ChatRoomDto.MultiResponse.class))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping
    public ResponseEntity<MultiResponseDto<ChatRoomDto.MultiResponse>> getChatRooms(@Parameter(name = "page", description = "조회할 페이지 번호 (1부터 시작)", example = "1")
                                                                                    @Positive @RequestParam int page,
                                                                                    @Parameter(name = "size", description = "한 페이지당 항목 수", example = "10")
                                                                                    @Positive @RequestParam int size) {
        AuthorizationUtils.isAdmin();
        Page<ChatRoom> chatRoomPage = chatRoomService.findChatRooms(page, size);
        List<ChatRoom> chatRooms = chatRoomPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(
                mapper.chatRoomsToChatRoomMultiResponses(chatRooms), chatRoomPage
        ), HttpStatus.OK);
    }

    // 로그인한 사용자가 참가했던 1대1 채팅방 전체조회
    @Operation(summary = "전체 1:1 채팅방 목록 조회", description = "로그인한 사용자가 이전에 참여했던 1:1 채팅방 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ChatRoomDto.MultiResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping("/my-chat-room/{member-id}")
    public ResponseEntity<MultiResponseDto<ChatRoomDto.MultiResponse>> getMyChatRooms(@Parameter(name = "member-id", description = "조회할 회원의 ID", example = "42")
                                                                                      @PathVariable("member-id") @Positive long memberId,
                                                                                      @Parameter(name = "page", description = "조회할 페이지 번호 (1부터 시작)", example = "1")
                                                                                      @Positive @RequestParam int page,
                                                                                      @Parameter(name = "size", description = "한 페이지당 항목 수", example = "10")
                                                                                      @Positive @RequestParam int size,
                                                                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Page<ChatRoom> chatRoomPage = chatRoomService.findMyCoupleChatRooms(page, size, memberId, customPrincipal.getMemberId());
        List<ChatRoom> chatRooms = chatRoomPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(
                mapper.chatRoomsToChatRoomMultiResponses(chatRooms), chatRoomPage
        ), HttpStatus.OK);
    }

    @Operation(summary = "채팅방 상세 조회", description = "특정 채팅방의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ChatRoomDto.SingleResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping("/{chat-room-id}")
    public ResponseEntity<SingleResponseDto<ChatRoomDto.SingleResponse>> getChatRoom(@Parameter(name = "chat-room-id", description = "조회할 채팅방의 ID", example = "15")
                                                                                     @PathVariable("chat-room-id") @Positive long chatRoomId,
                                                                                     @Parameter(name = "page", description = "조회할 페이지 번호 (1부터 시작)", example = "1")
                                                                                     @Positive @RequestParam int page,
                                                                                     @Parameter(name = "size", description = "한 페이지당 항목 수", example = "10")
                                                                                     @Positive @RequestParam int size,
                                                                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(chatRoomId);
        return new ResponseEntity(new SingleResponseDto<>(
                mapper.chatRoomToChatRoomSingleResponse(chatRoom, page, size)
        ), HttpStatus.OK);
    }

    // 채팅방 강제종료(상태 비활성화로 변경) - 관리자용
    @Operation(summary = "채팅방 강제 종료", description = "관리자가 특정 채팅방의 상태를 비활성화 상태로 변경합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "상태 변경 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @DeleteMapping("/office/{chat-room-id}")
    public ResponseEntity<Void> deleteChatRoom(@Parameter(name = "chat-room-id", description = "강제종료 대상 채팅방의 ID", example = "101")
                                               @PathVariable("chat-room-id") @Positive long chatRoomId) {
        AuthorizationUtils.isAdmin();
        chatRoomService.deleteChatRoom(chatRoomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 채팅방을 나가는 메서드
    @Operation(summary = "채팅방 나가기", description = "특정 회원을 채팅방에서 퇴장 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "퇴장 성공"),
                    @ApiResponse(responseCode = "403", description = "퇴장 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @DeleteMapping("/{chat-room-id}/{member-id}")
    public ResponseEntity<Void> exitFromChatRoom(@Parameter(name = "chat-room-id", description = "대상 채팅방의 ID", example = "101")
                                                 @PathVariable("chat-room-id") @Positive long chatRoomId,
                                                 @Parameter(name = "member-id", description = "탈퇴 대상 회원의 ID", example = "42")
                                                 @PathVariable("member-id") @Positive long memberId,
                                                 @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        AuthorizationUtils.isAdminOrOwner(memberId, customPrincipal.getMemberId());
        chatRoomService.exitChatPart(chatRoomId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 아이템 사용 후 채팅방을 나가는 메서드
    @Operation(summary = "채팅방 나가기 (아이템 사용 후)", description = "특정 회원을 채팅방에서 퇴장 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "퇴장 성공"),
                    @ApiResponse(responseCode = "403", description = "퇴장 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @DeleteMapping("/force/{chat-room-id}/{member-id}")
    public ResponseEntity<Void> forceExitFromChatRoom(@Parameter(name = "chat-room-id", description = "대상 채팅방의 ID", example = "101")
                                                      @PathVariable("chat-room-id") @Positive long chatRoomId,
                                                      @Parameter(name = "member-id", description = "탈퇴 대상 회원의 ID", example = "42")
                                                      @PathVariable("member-id") @Positive long memberId,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        AuthorizationUtils.isAdminOrOwner(memberId, customPrincipal.getMemberId());
        chatRoomService.forceExitChatPart(chatRoomId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 대기열을 등록할 때 참여중인 채팅방이 있는지 확인하는 API 엔드포인트
    @Operation(summary = "참여 가능 여부 확인", description = "회원이 채팅방 참여가 가능한지 여부를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "참여 가능 여부 반환 (true 또는 false)",
                            content = @Content(schema = @Schema(type = "boolean", example = "true"))),
                    @ApiResponse(responseCode = "403", description = "채팅 참여 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping("/isPossible/{member-id}")
    public ResponseEntity<SingleResponseDto<Boolean>> verifyChatPart(@Parameter(name = "member-id", description = "회원의 ID", example = "30")
                                                                     @PathVariable("member-id") long memberId) {
        return new ResponseEntity<>(new SingleResponseDto<>(chatRoomService.isMemberPossibleToPart(memberId)), HttpStatus.OK);
    }

    // 현재 참여중인 채팅방 있으면 ID 조회
    @Operation(summary = "현재 참여중인 채팅방 ID 조회", description = "회원이 현재 참여하고 있는 채팅방의 ID 를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "참여하고있는 채팅방의 ID 반환",
                            content = @Content(schema = @Schema(type = "boolean", example = "123"))),
                    @ApiResponse(responseCode = "404", description = "참여하고 있는 채팅방 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"NOTFOUND\", \"message\": \"Chatroom not found\"}")))}
    )
    @GetMapping("/curChatRoom")
    public ResponseEntity<Long> getCurChatRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        long curChatRoomId = chatRoomService.findCurChatRoom(customPrincipal.getMemberId());
        return new ResponseEntity<>(curChatRoomId, HttpStatus.OK);
    }


}
