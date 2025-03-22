package com.example.dice_talk.event.mapper;

import com.example.dice_talk.event.dto.EventDto;
import com.example.dice_talk.event.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "theme.themeId", source = "themeId")
    Event eventPostToEvent(EventDto.Post dto);
    Event eventPatchToEvent(EventDto.Patch dto);

    @Mapping(target = "themeId", source = "theme.themeId")
    EventDto.Response eventToEventResponse(Event event);
    List<EventDto.Response> eventsToEventResponses(List<Event> events);
}
