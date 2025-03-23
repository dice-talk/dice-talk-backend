package com.example.dice_talk.event.mapper;

import com.example.dice_talk.event.dto.RoomEventDto;
import com.example.dice_talk.event.entity.RoomEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomEventMapper {
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "chatRoom", ignore = true)
    RoomEvent roomEventPostDtoToRoomEvent(RoomEventDto.Post dto);

    RoomEventDto.Response roomEventToRoomEventResponseDto(RoomEvent roomEvent);
}
