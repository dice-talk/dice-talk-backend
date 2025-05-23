package com.example.dice_talk.dicelog.repository;

import com.example.dice_talk.dicelog.entity.DiceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiceLogRepository extends JpaRepository<DiceLog, Long> {
    Page<DiceLog> findAllByMember_MemberId(long memberId, Pageable pageable);
}
