package com.example.dice_talk.roomevent.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.ListResponseDto;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.roomevent.dto.RoomEventDto;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import com.example.dice_talk.roomevent.mapper.RoomEventMapper;
import com.example.dice_talk.roomevent.service.RoomEventService;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.net.URI;
import java.util.List;

@Tag(name = "RoomEvent", description = "채팅방 이벤트 API")
@SecurityRequirement(name = "JWT")
@RestController
@Validated
@RequestMapping("/room-event")
public class RoomEventController {
    private final static String ROOM_EVENT_DEFAULT_URL = ("/room-event");
    private final RoomEventService roomEventService;
    private final RoomEventMapper mapper;
    private final EventService eventService;

    public RoomEventController(RoomEventService roomEventService, RoomEventMapper mapper, EventService eventService) {
        this.roomEventService = roomEventService;
        this.mapper = mapper;
        this.eventService = eventService;
    }

    @Operation(summary = "채팅방 이벤트 결과 등록", description = "새로운 채팅방 이벤트 결과를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}"))),
            @ApiResponse(responseCode = "403", description = "등록 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")))
    })
    // 채팅방 내 이벤트 결과 등록
    @PostMapping
    public ResponseEntity<Void> postRoomEvent(@Parameter(description = "이벤트 생성 요청 DTO", required = true)
                                        @Valid @RequestBody RoomEventDto.Post postDto) {
        RoomEvent roomEvent = mapper.roomEventPostToRoomEvent(postDto);
        RoomEvent createdRoomEvent = roomEventService.createRoomEvent(roomEvent);
        URI location = UriCreator.createUri(ROOM_EVENT_DEFAULT_URL, createdRoomEvent.getRoomEventId());
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "채팅방 이벤트 목록 조회", description = "특정 채팅방의 이벤트 결과 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomEventDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}"))),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")))
    })
    // 채팅방 번호로 채팅방 조회 후 채팅방 내 이벤트 결과 전체 조회
    @GetMapping("/chat-room/{chat-room-id}")
    public ResponseEntity<ListResponseDto<RoomEventDto.Response>> getRoomEventsByChatRoom(@Parameter(description = "채팅방 ID", example = "1")
                                                  @PathVariable("chat-room-id") @Positive long chatRoomId) {
        List<RoomEvent> roomEvents = roomEventService.findRoomEventsByChatRoomId(chatRoomId);
        return new ResponseEntity<>(new ListResponseDto<>(mapper.roomEventsToResponses(roomEvents)), HttpStatus.OK);
    }

    @Operation(summary = "채팅방 이벤트 상세 조회", description = "특정 채팅방 이벤트 결과를 상세조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomEventDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}"))),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}"))),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")))
    })
    // 이벤트 상세 조회
    @GetMapping("/{room-event-id}")
    public ResponseEntity<SingleResponseDto<RoomEventDto.Response>> getAllRoomEventsByEvent(@Parameter(description = "채팅방 이벤트 ID", example = "1")
                                                  @PathVariable("room-event-id") @Positive long roomEventId) {
        RoomEvent roomEvent = roomEventService.findRoomEvent(roomEventId);
        RoomEventDto.Response response = mapper.roomEventToResponse(roomEvent);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @Operation(summary = "하트 히스토리 조회", description = "로그인한 회원의 하트 히스토리를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RoomEventDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}"))),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")))
    })
    // 하트 히스토리 조회
    @GetMapping("/history")
    public ResponseEntity<MultiResponseDto<RoomEventDto.Response>> getMyHeartHistory(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Page<RoomEvent> eventPage = roomEventService.findRoomEventsByEventAndMemberId(customPrincipal.getMemberId());
        List<RoomEvent> roomEvents = eventPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.roomEventsToResponses(roomEvents), eventPage), HttpStatus.OK);
    }


}
