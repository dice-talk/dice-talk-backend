package com.example.dice_talk.notification.event;

import com.example.dice_talk.answer.event.AnsweredEvent;
import com.example.dice_talk.item.event.ItemUsedEvent;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notice.event.NoticeCreatedEvent;
import com.example.dice_talk.notification.dto.NotificationDto;
import com.example.dice_talk.notification.entity.Notification;
import com.example.dice_talk.notification.mapper.NotificationMapper;
import com.example.dice_talk.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {
    private final NotificationService notificationService;
    private final NotificationMapper mapper;
    private final MemberService memberService;

    // 결제 완료 메서드는 후에 추가

    // QnA 답변등록 시
    @Async("notificationExecutor")
    @EventListener
    public void onAnswered(AnsweredEvent e) {
        String msg = "QnA 질문에 답변이 등록되었습니다.";
        NotificationDto.Post dto = new NotificationDto.Post(msg, e.getMemberId(), Notification.NotificationType.QNA);
        notificationService.createNotification(mapper.postToNotification(dto));
    }

    // 아이템 사용
    @Async("notificationExecutor")
    @EventListener
    public void onItemUsed(ItemUsedEvent e){
        String msg = String.format("%s 아이템을 사용하여 다이스가 %d개 사용되었습니다.", e.getItemName(), e.getQuantity());
        NotificationDto.Post dto = new NotificationDto.Post(msg, e.getMemberId(), Notification.NotificationType.DICE_USAGE);
        notificationService.createNotification(mapper.postToNotification(dto));
    }

    // 공지 등록
    // 우선적으로 개별 발송으로 처리한 후 이후에 -> 일괄발송 로직 구현 필요
    @Async("notificationExecutor")
    @EventListener
    public void onNoticeCreated(NoticeCreatedEvent e){
        String msg = String.format("새 공지사항이 등록되었습니다. : %s", e.getTitle());
        memberService.findAllActiveMember().stream().forEach(
                member -> {
                    NotificationDto.Post dto = new NotificationDto.Post(msg, member.getMemberId(), Notification.NotificationType.NOTICE_EVENT);
                    notificationService.createNotification(mapper.postToNotification(dto));
                }
        );
    }
}
