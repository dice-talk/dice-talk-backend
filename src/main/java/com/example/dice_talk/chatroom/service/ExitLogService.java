package com.example.dice_talk.chatroom.service;

import com.example.dice_talk.chatroom.repository.ExitLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ExitLogService {
    private final ExitLogRepository exitLogRepository;

    public boolean hasLeftToday(Long memberId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        return exitLogRepository.existsByMemberIdAndCreatedAtBetween(memberId, start, end);
    }
}
