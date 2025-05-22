package com.example.dice_talk.notice.repository;

import com.example.dice_talk.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // EVENT 타입이면서 ONGOING 상태인 notice 리스트 조회
    @Query("SELECT n FROM Notice n WHERE n.noticeType = :type AND n.noticeStatus = :status ORDER BY n.noticeImportance DESC, n.endDate ASC")
    List<Notice> findAllByNoticeTypeAndNoticeStatus(@Param("type") Notice.NoticeType noticeType, @Param("status") Notice.NoticeStatus status);
    //최신글 1개만 조회
    List<Notice> findTop1ByOrderByCreatedAtDesc();
}
