package com.example.dice_talk.event.repository;

import com.example.dice_talk.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByThemeId(long themeId);
}
