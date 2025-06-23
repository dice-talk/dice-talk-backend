package com.example.dice_talk.dicelog.repository;

import com.example.dice_talk.dicelog.entity.DiceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface DiceLogRepository extends JpaRepository<DiceLog, Long> {
    Page<DiceLog> findAllByMember_MemberId(long memberId, Pageable pageable);
    //관리자 대시보드 (사용자 item 결제 내역)
    int countByCreatedAtBetweenAndType(LocalDateTime start, LocalDateTime end, DiceLog.LogType type);
}
