package com.example.dice_talk.roomevent.mapper;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.event.entity.Event;
import com.example.dice_talk.roomevent.dto.RoomEventDto;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomEventMapper {
    default RoomEvent roomEventPostToRoomEvent(RoomEventDto.Post dto){
        RoomEvent roomEvent = new RoomEvent();
        roomEvent.setReceiverId(dto.getReceiverId());
        roomEvent.setSenderId(dto.getSenderId());
        Event event = new Event();
        event.setEventId(dto.getEventId());
        roomEvent.setEvent(event);
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(dto.getChatRoomId());
        roomEvent.setChatRoom(chatRoom);
        roomEvent.setRoomEventType(dto.getRoomEventType());
        if (dto.getRoomEventType().equals(RoomEvent.RoomEventType.PICK_MESSAGE)){
            roomEvent.setMessage(dto.getMessage());
        }
        return roomEvent;
    }

    default RoomEventDto.Response roomEventToResponse(RoomEvent roomEvent){
        RoomEventDto.Response response = new RoomEventDto.Response();
        response.setRoomEventId(roomEvent.getRoomEventId());
        response.setReceiverId(roomEvent.getReceiverId());
        response.setSenderId(roomEvent.getSenderId());
        response.setChatRoomId(roomEvent.getChatRoom().getChatRoomId());
        response.setRoomEventType(roomEvent.getRoomEventType());
        response.setThemeId(roomEvent.getChatRoom().getTheme().getThemeId());
        response.setCreatedAt(roomEvent.getCreatedAt());
        response.setModifiedAt(roomEvent.getModifiedAt());
        if (roomEvent.getRoomEventType().equals(RoomEvent.RoomEventType.PICK_MESSAGE)) {
            response.setMessage(roomEvent.getMessage());
        }
        return response;
    }

    default List<RoomEventDto.Response> roomEventsToResponses(List<RoomEvent> roomEvents){
        return roomEvents.stream().map(roomEvent ->
                roomEventToResponse(roomEvent)).collect(Collectors.toList());
    }
}
