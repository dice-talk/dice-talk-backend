package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ExitLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ExitLogRepository extends JpaRepository<ExitLog, Long> {
    boolean existsByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
