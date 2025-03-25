package com.example.dice_talk.roomevent.service;

import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import com.example.dice_talk.roomevent.repository.RoomEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomEventService {
    private final RoomEventRepository roomEventRepository;
    private final EventService eventService;
    private final ChatRoomService chatRoomService;

    public RoomEventService(RoomEventRepository roomEventRepository, EventService eventService, ChatRoomService chatRoomService) {
        this.roomEventRepository = roomEventRepository;
        this.eventService = eventService;
        this.chatRoomService = chatRoomService;
    }

    public RoomEvent createRoomEvent(RoomEvent roomEvent){
        eventService.findVerifiedEvent(roomEvent.getEvent().getEventId());
        chatRoomService.findVerifiedChatRoom(roomEvent.getChatRoom().getChatRoomId());
        return roomEventRepository.save(roomEvent);
    }

    public RoomEvent findRoomEvent(long roomEventId){
        return findVerifiedEvent(roomEventId);
    }

    public List<RoomEvent> findRoomEventsByChatRoomId(long chatRoomId){
        chatRoomService.findVerifiedChatRoom(chatRoomId);
        return roomEventRepository.findAllByChatRoom_ChatRoomId(chatRoomId);
    }

    public RoomEvent findVerifiedEvent(long eventId){
        return roomEventRepository.findById(eventId).orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.EVENT_NOT_FOUND));
    }
}
