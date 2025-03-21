package com.example.dice_talk.notice.repository;

import com.example.dice_talk.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
