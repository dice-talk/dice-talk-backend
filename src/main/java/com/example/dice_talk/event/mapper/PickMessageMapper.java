package com.example.dice_talk.event.mapper;

import com.example.dice_talk.event.dto.PickMessageDto;
import com.example.dice_talk.event.entity.PickMessage;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoomEventMapper.class})
public interface PickMessageMapper {
    @InheritConfiguration(name = "roomEventPostDtoToRoomEvent")
    PickMessage pickMessagePostDtoToPickMessage(PickMessageDto.Post dto);

    PickMessageDto.Response pickMessageToPickMessageResponseDto(PickMessage pickMessage);
}
