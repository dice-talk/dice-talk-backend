package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.entity.DiceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiceLogRepository extends JpaRepository<DiceLog, Long> {
    Page<DiceLog> findAllByMemberId(long memberId);
}
