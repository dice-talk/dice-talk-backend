package com.example.dice_talk.chat.repository;

import com.example.dice_talk.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
