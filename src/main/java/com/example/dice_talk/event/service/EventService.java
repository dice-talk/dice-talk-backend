package com.example.dice_talk.event.service;

import com.example.dice_talk.event.entity.Event;
import com.example.dice_talk.event.repository.EventRepository;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.theme.sevice.ThemeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final ThemeService themeService;

    public EventService(EventRepository eventRepository, ThemeService themeService) {
        this.eventRepository = eventRepository;
        this.themeService = themeService;
    }

    public Event createEvent(Event event){
        themeService.findVerifiedTheme(event.getTheme().getThemeId());
        return eventRepository.save(event);
    }

    public Event updateEvent(Event event){
        Event findEvent = findVerifiedEvent(event.getEventId());

        Optional.ofNullable(event.getEventName())
                .ifPresent(name -> findEvent.setEventName(name));
        Optional.ofNullable(event.getEventStatus())
                .ifPresent(status -> findEvent.setEventStatus(status));
        return eventRepository.save(findEvent);
    }

    public Event findEvent(long eventId){
        return findVerifiedEvent(eventId);
    }

    public Page<Event> findEvents(int page, int size){
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        return eventRepository.findAll(PageRequest.of(page-1, size, Sort.by("eventId").descending()));
    }

    public void deleteEvent(long eventId){
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.EVENT_NOT_FOUND));
        event.setEventStatus(Event.EventStatus.EVENT_CLOSE);
        eventRepository.save(event);
    }

    public Event findVerifiedEvent(long eventId){
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.EVENT_NOT_FOUND));
    }

    // themeId를 통해 theme에 해당하는 event를 조회하는 메서드
    public List<Event> findEventsByThemeId(long themeId){
        return findEventsByThemeId(themeId);
    }
}
