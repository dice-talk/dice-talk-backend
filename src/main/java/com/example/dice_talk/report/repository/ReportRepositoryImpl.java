package com.example.dice_talk.report.repository;

import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.mapper.ChatMapper;
import com.example.dice_talk.chat.service.ChatService;
import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.dashboard.dto.QDailyCountDto;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.MemberRepository;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.QReport;
import com.example.dice_talk.report.entity.Report;
import com.example.dice_talk.report.mapper.ReportMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.mail.search.SearchTerm;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {
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

    //웹페이지 : 금일 신고 건수 조회
    public int countReports(LocalDateTime start, LocalDateTime end) {
        QReport report = QReport.report;

        Long count = queryFactory
                .select(report.count())
                .from(report)
                .where(report.createdAt.between(start, end))
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }

    //웹페이지 : 주간 신고 건수 조회
    @Override
    public List<DailyCountDto> countReportsByDate(LocalDateTime start, LocalDateTime end) {
        QReport report = QReport.report;


        DateExpression<Date> dateOnly = Expressions.dateTemplate(
                Date.class, "DATE({0})", report.createdAt);

        List<Tuple> tuples = queryFactory
                .select(dateOnly, report.reportId.count())
                .from(report)
                .where(report.createdAt.between(start, end))
                .groupBy(dateOnly)
                .orderBy(dateOnly.asc())
                .fetch();

        return tuples.stream()
                .map(t -> {
                    Date sqlDate = t.get(dateOnly);
                    Long cnt = t.get(report.reportId.count());
                    LocalDate date = (sqlDate != null ? sqlDate.toLocalDate() : null);
                    return new DailyCountDto(
                            date,
                            cnt != null ? cnt : 0L
                    );
                }).collect(Collectors.toList());
    }
//        DateExpression<LocalDate> dateOnly = Expressions.dateTemplate(
//                LocalDate.class, "DATE({0})", report.createdAt);
//

    /// /        NumberExpression<Long> countExpr = report.count();
//        return queryFactory
//                .select(new QDailyCountDto(dateOnly, report.count()))
//                .from(report)
//                .where(report.createdAt.between(start, end))
//                .groupBy(dateOnly)
//                .orderBy(dateOnly.asc())
//                .fetch();
//    }
    @Override
    public Page<ReportDto.Response> searchByIdOrEmailAndStatus(String search, String reportStatus, Pageable pageable) {
        QReport report = QReport.report;
        BooleanBuilder builder = new BooleanBuilder();

        // 신고 상태 조건
        if (reportStatus != null && !reportStatus.isBlank()) {
            builder.and(report.reportStatus.eq(Report.ReportStatus.valueOf(reportStatus)));
        }

        // ID/이메일(일부) 조건
        if (search != null && !search.isBlank()) {
            if (search.matches("\\d+")) {
                // 숫자면 memberId로만 검색
                Long id = Long.valueOf(search);
                builder.and(report.reporterId.eq(id).or(report.reportedMemberId.eq(id)));
            } else {
                // 문자면 이메일 일부로 memberId 리스트 조회
                List<Member> members = memberRepository.findByEmailContaining(search);
                List<Long> memberIds = members.stream().map(Member::getMemberId).collect(Collectors.toList());
                if (!memberIds.isEmpty()) {
                    builder.and(report.reporterId.in(memberIds).or(report.reportedMemberId.in(memberIds)));
                } else {
                    // 검색 결과 없으면 빈 결과 반환
                    return new PageImpl<>(List.of(), pageable, 0);
                }
            }
        }

        // 1. Report 조회
        List<Report> reports = queryFactory
                .selectFrom(report)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(report.createdAt.desc())
                .fetch();

        // 2. Member ID 수집
        Set<Long> memberIds = reports.stream()
                .flatMap(r -> Arrays.asList(r.getReporterId(), r.getReportedMemberId()).stream())
                .collect(Collectors.toSet());

        // 3. Member 정보 한 번에 조회
        Map<Long, String> memberEmailMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getEmail));

        // 4. DTO 변환
        List<ReportDto.Response> reportDtos = reports.stream()
                .map(r -> ReportDto.Response.from(
                        r,
                        memberEmailMap.get(r.getReporterId()),
                        memberEmailMap.get(r.getReportedMemberId())
                ))
                .collect(Collectors.toList());

        // 5. 전체 개수 조회
        Long total = queryFactory
                .select(report.count())
                .from(report)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(reportDtos, pageable, total);
    }
}
