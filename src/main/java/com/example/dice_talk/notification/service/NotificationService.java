package com.example.dice_talk.notification.service;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notification.entity.Notification;
import com.example.dice_talk.notification.repository.NotificationRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberService memberService;

    public NotificationService(NotificationRepository notificationRepository, MemberService memberService) {
        this.notificationRepository = notificationRepository;
        this.memberService = memberService;
    }

    public Notification createNotification(Notification notification){
        // member를 비정규화 했기 때문에 영속성 컨텍스트에 없는 경우가 존재함.
//        Member reportedMember = memberService.findVerifiedMember(notification.getMember().getMemberId());
//        notification.setMember(reportedMember);
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

    // 알림 선택 삭제
    @Transactional
    public void deleteNotification(Long memberId, Long notificationId){
        verifyExistNotification(notificationId);
        notificationRepository.deleteById(notificationId);
    }

    // 특정 멤버의 알림 전체 삭제
    @Transactional
    public void deleteAllNotification(Long memberId){
        notificationRepository.deleteAllByMember_MemberId(memberId);
    }

    // 존재하는 알림인지 확인
    public void verifyExistNotification(Long notificationId){
        notificationRepository.findById(notificationId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.NOTIFICATION_NOT_FOUND));
    }

    // pk 로 해당 Notification 찾기
    public Notification findVerifiedNotification(Long notificationId){
        return notificationRepository.findById(notificationId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.NOTIFICATION_NOT_FOUND));
    }
}
