package com.example.dice_talk.notification.service;

import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notification.entity.Notification;
import com.example.dice_talk.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberService memberService;

    public NotificationService(NotificationRepository notificationRepository, MemberService memberService) {
        this.notificationRepository = notificationRepository;
        this.memberService = memberService;
    }

    public Notification createNotification(Notification notification){
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long memberId){
        notificationRepository.markAllAsRead(memberId);
    }

    // 전체 조회(페이지네이션)
    @Transactional
    public Page<Notification> getNotifications(Long memberId, Pageable pageable){
        return notificationRepository.findByMember_MemberIdOrderByCreatedAtDesc(memberId, pageable);
    }

    // 읽지 않은 알림 카운트
    @Transactional
    public long countUnread(Long memberId){
        return notificationRepository.countByMember_MemberIdAndIsReadFalse(memberId);
    }
}
