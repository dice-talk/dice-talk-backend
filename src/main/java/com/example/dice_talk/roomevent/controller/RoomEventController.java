package com.example.dice_talk.roomevent.controller;

import com.example.dice_talk.dto.ListResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.roomevent.dto.RoomEventDto;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import com.example.dice_talk.roomevent.mapper.RoomEventMapper;
import com.example.dice_talk.roomevent.service.RoomEventService;
import com.example.dice_talk.utils.UriCreator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public RoomEventController(RoomEventService roomEventService, RoomEventMapper mapper) {
        this.roomEventService = roomEventService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postRoomEvent(@Valid @RequestBody RoomEventDto.Post postDto){
        RoomEvent roomEvent = mapper.roomEventPostToRoomEvent(postDto);
        RoomEvent createdRoomEvent = roomEventService.createRoomEvent(roomEvent);
        URI location = UriCreator.createUri(ROOM_EVENT_DEFAULT_URL, createdRoomEvent.getRoomEventId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/chat-room/{chat-room-id}")
    public ResponseEntity getRoomEventsByChatRoom(@PathVariable("chat-room-id") @Positive long chatRoomId){
        List<RoomEvent> roomEvents = roomEventService.findRoomEventsByChatRoomId(chatRoomId);
        return new ResponseEntity(new ListResponseDto<>(roomEvents), HttpStatus.OK);
    }

    // 이벤트 상세 조회
    @GetMapping("/{room-event-id}")
    public ResponseEntity getAllRoomEventsByEvent(@PathVariable("room-event-id") @Positive long roomEventId){
        RoomEvent roomEvent = roomEventService.findRoomEvent(roomEventId);
        RoomEventDto.Response response = mapper.roomEventToResponse(roomEvent);
        return new ResponseEntity(new SingleResponseDto<>(response), HttpStatus.OK);
    }




}
