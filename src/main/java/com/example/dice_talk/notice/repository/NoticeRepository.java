package com.example.dice_talk.notice.repository;

import com.example.dice_talk.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT n FROM Notice n WHERE n.noticeType = :type AND n.noticeStatus = :status ORDER BY n.noticeImportance DESC, n.endDate ASC")
    List<Notice> findAllByNoticeTypeAndNoticeStatus(@Param("type") Notice.NoticeType noticeType, @Param("status") Notice.NoticeStatus status);
}
