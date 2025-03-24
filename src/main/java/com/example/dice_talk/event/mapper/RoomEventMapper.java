package com.example.dice_talk.event.mapper;

import com.example.dice_talk.event.dto.RoomEventDto;
import com.example.dice_talk.event.entity.PickMessage;
import com.example.dice_talk.event.entity.RoomEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomEventMapper {
    // 수정해야함
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "chatRoom", ignore = true)
    PickMessage roomEventPostDtoToRoomEvent(RoomEventDto.Post dto);

    RoomEventDto.Response roomEventToRoomEventResponseDto(RoomEvent roomEvent);
}
