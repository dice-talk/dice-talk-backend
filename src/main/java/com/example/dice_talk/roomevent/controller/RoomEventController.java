package com.example.dice_talk.roomevent.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.ListResponseDto;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.roomevent.dto.RoomEventDto;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import com.example.dice_talk.roomevent.mapper.RoomEventMapper;
import com.example.dice_talk.roomevent.service.RoomEventService;
import com.example.dice_talk.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
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

    // 채팅방 내 이벤트 결과 등록
    @PostMapping
    public ResponseEntity postRoomEvent(@Valid @RequestBody RoomEventDto.Post postDto){
        RoomEvent roomEvent = mapper.roomEventPostToRoomEvent(postDto);
        RoomEvent createdRoomEvent = roomEventService.createRoomEvent(roomEvent);
        URI location = UriCreator.createUri(ROOM_EVENT_DEFAULT_URL, createdRoomEvent.getRoomEventId());
        return ResponseEntity.created(location).build();
    }

    // 채팅방 번호로 채팅방 조회 후 채팅방 내 이벤트 결과 전체 조회
    @GetMapping("/chat-room/{chat-room-id}")
    public ResponseEntity getRoomEventsByChatRoom(@PathVariable("chat-room-id") @Positive long chatRoomId){
        List<RoomEvent> roomEvents = roomEventService.findRoomEventsByChatRoomId(chatRoomId);
        return new ResponseEntity<>(new ListResponseDto<>(mapper.roomEventsToResponses(roomEvents)), HttpStatus.OK);
    }

    // 이벤트 상세 조회
    @GetMapping("/{room-event-id}")
    public ResponseEntity getAllRoomEventsByEvent(@PathVariable("room-event-id") @Positive long roomEventId){
        RoomEvent roomEvent = roomEventService.findRoomEvent(roomEventId);
        RoomEventDto.Response response = mapper.roomEventToResponse(roomEvent);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    // 하트 히스토리 조회
    @GetMapping("/history/{member-id}")
    public ResponseEntity getMyHeartHistory(@PathVariable("member-id") long memberId,
                                            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Page<RoomEvent> eventPage = roomEventService.findRoomEventsByEventAndMemberId(memberId, customPrincipal.getMemberId());
        List<RoomEvent> roomEvents = eventPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.roomEventsToResponses(roomEvents), eventPage), HttpStatus.OK);
    }




}
