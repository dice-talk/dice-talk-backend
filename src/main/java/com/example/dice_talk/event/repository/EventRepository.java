package com.example.dice_talk.event.repository;

import com.example.dice_talk.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
