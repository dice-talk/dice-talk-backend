package com.example.dice_talk.event.repository;

import com.example.dice_talk.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByTheme_ThemeId(long themeId);
    Event findByEventName(String eventName);
    Page<Event> findByEventStatus(Event.EventStatus status, Pageable pageable);
    Page<Event> findByTheme_ThemeId(Long themeId, Pageable pageable);
    Page<Event> findByEventStatusAndTheme_ThemeId(Event.EventStatus status, Long themeId, Pageable pageable);
}
