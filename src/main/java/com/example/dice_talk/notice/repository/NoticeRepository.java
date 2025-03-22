package com.example.dice_talk.notice.repository;

import com.example.dice_talk.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
