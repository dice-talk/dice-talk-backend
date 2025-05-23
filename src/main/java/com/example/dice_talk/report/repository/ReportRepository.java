package com.example.dice_talk.report.repository;

import com.example.dice_talk.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    int countByReportedMemberIdAndReportStatus(Long reportedMemberId, Report.ReportStatus status);
}
