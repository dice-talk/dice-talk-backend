package com.example.dice_talk.report.repository;

import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReportRepositoryCustom {
    // 페이지네이션된 신고 목록 조회 (회원 이메일 포함)
    Page<ReportDto.Response> findAllWithMemberEmail(Pageable pageable);

    // 단일 신고 상세 조회 (회원 이메일 포함)
    ReportDto.Response findByIdWithMemberEmail(Long reportId);

    // 특정 회원의 처리완료된 신고 내역 조회
    List<ReportDto.Response> findAllByMember_MemberIdAndReportStatusWithEmail(Long memberId, Report.ReportStatus reportStatus);

    // 특정 회원의 신고 목록 조회 (선택적)
    Page<ReportDto.Response> findByMemberIdWithEmail(Long memberId, Pageable pageable);

    // 특정 상태의 신고 목록 조회 (선택적)
    Page<ReportDto.Response> findByStatusWithMemberEmail(Report.ReportStatus status, Pageable pageable);
}
