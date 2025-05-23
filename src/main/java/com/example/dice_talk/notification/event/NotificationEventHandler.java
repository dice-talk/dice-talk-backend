package com.example.dice_talk.notification.event;

import com.example.dice_talk.answer.event.AnsweredEvent;
import com.example.dice_talk.item.event.ItemUsedEvent;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notice.event.NoticeCreatedEvent;
import com.example.dice_talk.notification.dto.NotificationDto;
import com.example.dice_talk.notification.entity.Notification;
import com.example.dice_talk.notification.mapper.NotificationMapper;
import com.example.dice_talk.notification.service.NotificationService;
import com.example.dice_talk.report.event.ReportCompletedEvent;
import com.example.dice_talk.report.event.ReportCreatedEvent;
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

    // 신고 등록
    @Async("notificationExecutor")
    @EventListener
    public void onReportCreated(ReportCreatedEvent e){
        String msg = String.format("신고가 접수되었습니다. 사유 : %s", e.getReason());
        NotificationDto.Post dto = new NotificationDto.Post(msg, e.getReporterId(), Notification.NotificationType.REPORT);
        notificationService.createNotification(mapper.postToNotification(dto));
    }

    // 신고 처리 완료
    @Async("notificationExecutor")
    @EventListener
    public void onReportCompleted(ReportCompletedEvent e){
        String msg = String.format(
                "[경고 알림]\n" +
                        "• 일시: %s\n" +               // {날짜} → %s
                        "• 사유: %s\n" +               // {사유} → %s
                        "• 누적 경고 횟수: %d회\n\n" + // {횟수} → %d + "회"
                        "안녕하세요, 회원님.\n" +
                        "위 사유로 경고가 누적되었습니다.\n" +
                        "경고 3회 누적 시 계정이 정지되오니\n" +
                        "규정 준수 부탁드립니다.",
                e.getDate(), e.getReason(), e.getWarnCount()    // 순서대로 포맷 인자 전달
        );
        NotificationDto.Post dto = new NotificationDto.Post(msg, e.getReportedMemberId(), Notification.NotificationType.REPORT);
        notificationService.createNotification(mapper.postToNotification(dto));
    }
}
