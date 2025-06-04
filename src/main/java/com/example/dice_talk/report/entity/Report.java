package com.example.dice_talk.report.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.report.repository.ReportRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private Long reporterId;

    @Column(nullable = false)
    private Long reportedMemberId;

    @Enumerated(EnumType.STRING)
    private ReportReason reportReason = ReportReason.HARASSMENT;

    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus =ReportStatus.REPORT_RECEIVED;

    @OneToMany(mappedBy = "report", cascade = CascadeType.PERSIST)
    private List<ChatReport> chatReports = new ArrayList<>();

    public enum ReportStatus{
        REPORT_RECEIVED("신고 접수"),
        REPORT_REJECTED("신고 반려"),
        REPORT_COMPLETED("처리 완료"),
        REPORT_DELETED("신고 삭제");

        private String status;

        ReportStatus(String status) {
            this.status = status;
        }
    }

    @Getter
    public enum ReportReason{
        SPAM("스팸성 메시지 (광고·홍보 등)"),
        HARASSMENT("욕설/괴롭힘 (언어폭력, 비하 발언 등)"),
        SCAM("사기/피싱 (금전 요구, 피싱 URL 등)"),
        ABUSE("폭력/혐오 발언 (폭력적 언어, 혐오 표현 등)"),
        PRIVACY_VIOLATION("개인정보 노출 (전화번호·주소·민감정보 등");

        private String description;

        ReportReason(String description) { this.description = description; }
    }

    public void setChatReport(ChatReport chatReport){
        if(chatReport.getReport() != this){
            chatReport.setReport(this);
        }
        if(!this.getChatReports().contains(chatReport)){
            this.getChatReports().add(chatReport);
        }
    }
}
