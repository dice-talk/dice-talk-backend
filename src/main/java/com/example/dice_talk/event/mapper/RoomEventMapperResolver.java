package com.example.dice_talk.event.mapper;

import com.example.dice_talk.event.dto.PickMessageDto;
import com.example.dice_talk.event.dto.RoomEventDto;
import com.example.dice_talk.event.entity.PickMessage;
import com.example.dice_talk.event.entity.RoomEvent;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", uses = {PickMessageMapper.class, RoomEventMapper.class})
public interface RoomEventMapperResolver {

    @SubclassMapping(source = PickMessage.class, target = PickMessageDto.Response.class)
    RoomEventDto.Response roomEventToDto(RoomEvent roomEvent);

    default RoomEventDto.Response mapByType(RoomEvent roomEvent) {
        if (roomEvent == null) {
            return null;
        }

        // 이벤트 타입에 따라 적절한 매퍼 호출
        if (roomEvent instanceof PickMessage) {
            PickMessage pickMessage = (PickMessage) roomEvent;
            return pickMessageToPickMessageResponseDto(pickMessage);
        }

        // 기본 매핑
        return roomEventToRoomEventResponseDto(roomEvent);
    }

    // 다른 매퍼의 메서드를 직접 호출
    PickMessageDto.Response pickMessageToPickMessageResponseDto(PickMessage pickMessage);

    RoomEventDto.Response roomEventToRoomEventResponseDto(RoomEvent roomEvent);
}