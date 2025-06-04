package com.example.dice_talk.report.repository;

import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.mapper.ChatMapper;
import com.example.dice_talk.chat.service.ChatService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.MemberRepository;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.QReport;
import com.example.dice_talk.report.entity.Report;
import com.example.dice_talk.report.mapper.ReportMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.mail.search.SearchTerm;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final MemberRepository memberRepository;
    private final ChatService chatService;
    private final ChatMapper chatMapper;
    private final ReportMapper reportMapper;

    @Override
    public Page<ReportDto.Response> findAllWithMemberEmail(Pageable pageable) {
        // 1. Report 조회
        List<Report> reports = queryFactory
                .selectFrom(QReport.report)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QReport.report.createdAt.desc())
                .fetch();

        // 2. Member ID 수집
        Set<Long> memberIds = reports.stream()
                .flatMap(report -> Arrays.asList(report.getReporterId(), report.getReportedMemberId()).stream())
                .collect(Collectors.toSet());

        // 3. Member 정보 한 번에 조회
        Map<Long, String> memberEmailMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getEmail));

        // 4. DTO 변환
        List<ReportDto.Response> reportDtos = reports.stream()
                .map(report -> {
                    ReportDto.Response response = ReportDto.Response.from(
                            report,
                            memberEmailMap.get(report.getReporterId()),
                            memberEmailMap.get(report.getReportedMemberId())
                    );

                    // 5. 채팅 정보 추가
                    List<Chat> reportedChats = report.getChatReports().stream()
                            .map(chatReport -> chatService.findChat(chatReport.getChat().getChatId()))
                            .collect(Collectors.toList());

                    if (!reportedChats.isEmpty()) {
                        response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
                        response.setChatRoomId(reportedChats.get(0).getChatRoom().getChatRoomId());
                    }

                    return response;
                })
                .collect(Collectors.toList());

        // 6. 전체 개수 조회
        Long total = queryFactory
                .select(QReport.report.count())
                .from(QReport.report)
                .fetchOne();

        return new PageImpl<>(reportDtos, pageable, total);
    }

    @Override
    public ReportDto.Response findByIdWithMemberEmail(Long reportId) {
        // 1. Report 조회
        Report report = queryFactory
                .selectFrom(QReport.report)
                .where(QReport.report.reportId.eq(reportId))
                .fetchOne();

        if (report == null) {
            throw new BusinessLogicException(ExceptionCode.REPORT_NOT_FOUND);
        }

        // 2. Member 정보 조회
        List<Member> members = memberRepository.findAllById(
                Arrays.asList(report.getReporterId(), report.getReportedMemberId())
        );

        Map<Long, String> memberEmailMap = members.stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getEmail));

        // 3. DTO 변환
        ReportDto.Response response = ReportDto.Response.from(
                report,
                memberEmailMap.get(report.getReporterId()),
                memberEmailMap.get(report.getReportedMemberId())
        );

        // 4. 채팅 정보 추가
        List<Chat> reportedChats = report.getChatReports().stream()
                .map(chatReport -> chatService.findChat(chatReport.getChat().getChatId()))
                .collect(Collectors.toList());

        if (!reportedChats.isEmpty()) {
            response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
            response.setChatRoomId(reportedChats.get(0).getChatRoom().getChatRoomId());
        }

        return response;
    }

    @Override
    public Page<ReportDto.Response> findByMemberIdWithEmail(Long memberId, Pageable pageable) {
        // 1. Report 조회 (신고자 또는 피신고자 ID로 조회)
        List<Report> reports = queryFactory
                .selectFrom(QReport.report)
                .where(QReport.report.reporterId.eq(memberId)
                        .or(QReport.report.reportedMemberId.eq(memberId)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QReport.report.createdAt.desc())
                .fetch();

        // 2. Member ID 수집
        Set<Long> memberIds = reports.stream()
                .flatMap(report -> Arrays.asList(report.getReporterId(), report.getReportedMemberId()).stream())
                .collect(Collectors.toSet());

        // 3. Member 정보 한 번에 조회
        Map<Long, String> memberEmailMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getEmail));

        // 4. DTO 변환
        List<ReportDto.Response> reportDtos = reports.stream()
                .map(report -> {
                    ReportDto.Response response = ReportDto.Response.from(
                            report,
                            memberEmailMap.get(report.getReporterId()),
                            memberEmailMap.get(report.getReportedMemberId())
                    );

                    // 5. 채팅 정보 추가
                    List<Chat> reportedChats = report.getChatReports().stream()
                            .map(chatReport -> chatService.findChat(chatReport.getChat().getChatId()))
                            .collect(Collectors.toList());

                    if (!reportedChats.isEmpty()) {
                        response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
                    }

                    return response;
                })
                .collect(Collectors.toList());

        // 6. 전체 개수 조회
        Long total = queryFactory
                .select(QReport.report.count())
                .from(QReport.report)
                .where(QReport.report.reporterId.eq(memberId)
                        .or(QReport.report.reportedMemberId.eq(memberId)))
                .fetchOne();

        return new PageImpl<>(reportDtos, pageable, total);
    }

    @Override
    public Page<ReportDto.Response> findByStatusWithMemberEmail(Report.ReportStatus status, Pageable pageable) {
        // 1. Report 조회 (상태로 필터링)
        List<Report> reports = queryFactory
                .selectFrom(QReport.report)
                .where(QReport.report.reportStatus.eq(status))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QReport.report.createdAt.desc())
                .fetch();

        // 2. Member ID 수집
        Set<Long> memberIds = reports.stream()
                .flatMap(report -> Arrays.asList(report.getReporterId(), report.getReportedMemberId()).stream())
                .collect(Collectors.toSet());

        // 3. Member 정보 한 번에 조회
        Map<Long, String> memberEmailMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getEmail));

        // 4. DTO 변환
        List<ReportDto.Response> reportDtos = reports.stream()
                .map(report -> {
                    ReportDto.Response response = ReportDto.Response.from(
                            report,
                            memberEmailMap.get(report.getReporterId()),
                            memberEmailMap.get(report.getReportedMemberId())
                    );

                    // 5. 채팅 정보 추가
                    List<Chat> reportedChats = report.getChatReports().stream()
                            .map(chatReport -> chatService.findChat(chatReport.getChat().getChatId()))
                            .collect(Collectors.toList());

                    if (!reportedChats.isEmpty()) {
                        response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
                    }

                    return response;
                })
                .collect(Collectors.toList());

        // 6. 전체 개수 조회
        Long total = queryFactory
                .select(QReport.report.count())
                .from(QReport.report)
                .where(QReport.report.reportStatus.eq(status))
                .fetchOne();

        return new PageImpl<>(reportDtos, pageable, total);
    }

    @Override
    public List<ReportDto.Response> findAllByMember_MemberIdAndReportStatusWithEmail(Long memberId, Report.ReportStatus reportStatus) {
        // 1. Report 조회 (회원 ID와 상태로 필터링)
        List<Report> reports = queryFactory
                .selectFrom(QReport.report)
                .where(QReport.report.reportedMemberId.eq(memberId)
                        .and(QReport.report.reportStatus.eq(reportStatus)))
                .orderBy(QReport.report.createdAt.desc())
                .fetch();

        // 2. Member ID 수집
        Set<Long> memberIds = reports.stream()
                .flatMap(report -> Arrays.asList(report.getReporterId(), report.getReportedMemberId()).stream())
                .collect(Collectors.toSet());

        // 3. Member 정보 한 번에 조회
        Map<Long, String> memberEmailMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getEmail));

        // 4. DTO 변환
        return reports.stream()
                .map(report -> {
                    ReportDto.Response response = ReportDto.Response.from(
                            report,
                            memberEmailMap.get(report.getReporterId()),
                            memberEmailMap.get(report.getReportedMemberId())
                    );

                    // 5. 채팅 정보 추가
                    List<Chat> reportedChats = report.getChatReports().stream()
                            .map(chatReport -> chatService.findChat(chatReport.getChat().getChatId()))
                            .collect(Collectors.toList());

                    if (!reportedChats.isEmpty()) {
                        response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }
}
