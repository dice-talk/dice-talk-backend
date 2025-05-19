package com.example.dice_talk.notice.repository;

import com.example.dice_talk.notice.entity.NoticeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeImageRepository extends JpaRepository<NoticeImage, Long> {
    List<NoticeImage> findAllByNotice_NoticeId (Long noticeId);
}
