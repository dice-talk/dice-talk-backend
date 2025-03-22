package com.example.dice_talk.report.service;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.report.entity.Report;
import com.example.dice_talk.report.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
//    private final ChatService chatService;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report createReport(Report report, long memberId){
        // 존재하는 chatId 인지 검증
        // verifyExistsChats(report.getChatReports());
        report.setReporterId(memberId);
        return reportRepository.save(report);
    }

    public Report findReport (long reportId){
        return findVerifiedReport(reportId);
    }

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

    public Report findVerifiedReport(long reportId){
        Optional<Report> optionalReport = reportRepository.findById(reportId);
        return optionalReport.orElseThrow(() -> (
                new BusinessLogicException(ExceptionCode.REPORT_NOT_FOUND)
                ));
    }
}
