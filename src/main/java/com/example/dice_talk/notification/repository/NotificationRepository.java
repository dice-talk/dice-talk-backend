package com.example.dice_talk.notification.repository;

import com.example.dice_talk.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByMember_MemberIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByMember_MemberIdAndIsReadFalse(Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n " +
            "SET n.isRead = true " +
            "WHERE n.member.memberId = :memberId AND n.isRead = false")
    void markAllAsRead(@Param("memberId") Long memberId);

    void deleteAllByMember_MemberId(Long memberId);
}
