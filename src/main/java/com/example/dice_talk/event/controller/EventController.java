package com.example.dice_talk.event.controller;

import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.event.dto.EventDto;
import com.example.dice_talk.event.entity.Event;
import com.example.dice_talk.event.mapper.EventMapper;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.theme.dto.ThemeDto;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final static String EVENT_DEFAULT_URL = "/events";
    private final EventService eventService;
    private final EventMapper mapper;

    public EventController(EventService eventService, EventMapper mapper) {
        this.eventService = eventService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postEvent(@Valid @RequestBody EventDto.Post postDto){
        Event event = mapper.eventPostToEvent(postDto);
        Event createdEvent = eventService.createEvent(event);
        URI location = UriCreator.createUri(EVENT_DEFAULT_URL, createdEvent.getEventId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{event-id}")
    public ResponseEntity patchEvent(
            @PathVariable("event-id") @Positive long eventId,
            @Valid @RequestBody EventDto.Patch patchDto
    ){
        patchDto.setEventId(eventId);
        Event event = eventService.updateEvent(mapper.eventPatchToEvent(patchDto));
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.eventToEventResponse(event)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getEvents(@Positive @RequestParam int page, @Positive @RequestParam int size){
        Page<Event> eventPage = eventService.findEvents(page, size);
        List<Event> events = eventPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.eventsToEventResponses(events), eventPage), HttpStatus.OK);
    }

    @GetMapping("/{event-id}")
    public ResponseEntity getEvent(@PathVariable("event-id") @Positive long eventId){
        Event event = eventService.findEvent(eventId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.eventToEventResponse(event)), HttpStatus.OK);
    }

    @DeleteMapping("/{event-id}")
    public ResponseEntity deleteEvent(@PathVariable("event-id") long eventId){
        eventService.deleteEvent(eventId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
