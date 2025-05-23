package com.example.dice_talk.report.service;

import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.service.ChatService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.report.entity.Report;
import com.example.dice_talk.report.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberService memberService;
    private final ChatService chatService;

    public ReportService(ReportRepository reportRepository, MemberService memberService, ChatService chatService) {
        this.reportRepository = reportRepository;
        this.memberService = memberService;
        this.chatService = chatService;
    }

    @Transactional
    public void createReports(List<Report> reports){
        reports.stream().forEach(
                report -> reportRepository.save(report)
        );
    }

    // 신고 처리완료
    public Report completeReport(long reportId){
        Report report = findVerifiedReport(reportId);
        report.setReportStatus(Report.ReportStatus.REPORT_COMPLETED);
        Report saved = reportRepository.save(report);
        long count = reportRepository.countByReportedMemberIdAndReportStatus(saved.getReportedMemberId(), Report.ReportStatus.REPORT_COMPLETED);
        if (count >= 3){
            memberService.banMember(saved.getReportedMemberId());
        }
        return saved;
    }

    // 신고 반려
    public Report rejectReport(long reportId){
        Report report = findVerifiedReport(reportId);
        report.setReportStatus(Report.ReportStatus.REPORT_REJECTED);
        return reportRepository.save(report);
    }

    // 신고 전체조회(페이지네이션)
    public Page<Report> findReports (int page, int size){
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        return reportRepository.findAll(PageRequest.of(page-1, size, Sort.by("reportId").descending()));
    }

    // 상태만 변경
    public void deleteReport (long reportId){
        Report report = findVerifiedReport(reportId);
        report.setReportStatus(Report.ReportStatus.REPORT_COMPLETED);
        reportRepository.save(report);
    }

    // 신고 조회 시 함께 신고한 채팅이 있다면 같이 반환
    public List<Chat> findReportDetails(long reportId){
        Report report = findVerifiedReport(reportId);
        List<Chat> reportedChats = new ArrayList<>();
        if(!report.getChatReports().isEmpty()){
            report.getChatReports().stream().forEach(
                    chatReport -> {
                        Chat foundChat = chatService.findChat(chatReport.getChat().getChatId());
                        reportedChats.add(foundChat);
                    }
            );
        }
        return reportedChats;
    }

    public Report findVerifiedReport(long reportId){
        Optional<Report> optionalReport = reportRepository.findById(reportId);
        return optionalReport.orElseThrow(() -> (
                new BusinessLogicException(ExceptionCode.REPORT_NOT_FOUND)
                ));
    }
}
