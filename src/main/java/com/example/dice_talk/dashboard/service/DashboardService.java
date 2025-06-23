package com.example.dice_talk.dashboard.service;

import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.dashboard.dto.*;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notice.service.NoticeService;
import com.example.dice_talk.payment.service.PaymentService;
import com.example.dice_talk.question.service.QuestionService;
import com.example.dice_talk.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final MemberService memberService;
    private final QuestionService questionService;
    private final NoticeService noticeService;
    private final ChatRoomService chatRoomService;
    private final ReportService reportService;
    private final PaymentService paymentService;


    public MainDashboardResponseDto findVerifiedExistsDashBoard() {

        //오늘 회원가입한 회원의 이름
        List<String> newMemberNames = memberService.findTodayRegisteredMembers();

        //문의글 관리 : 조회일 기준 최근 7일간 등록된 질문 수 + 미답변 질문 수
        List<DashboardQuestion> dashboardQuestions = questionService.findUnansweredQuestions();

        //공지+이벤트 관리 = 최근 등록된 공지 + 진행중인 이벤트
        List<DashboardNotice> dashboardNotices = noticeService.findRecentNotices();

        //채팅방 관리 : 잔행중인 채팅방(단체 /1대1)
        List<DashboardChatRoom> chatRooms = chatRoomService.activeChatRoomCount();

        //결제 관리
        List<DashboardPayment> dashboardPayments = paymentService.getDashboardPayments();

        return dashBoardToResponse(newMemberNames, dashboardQuestions, dashboardNotices, chatRooms, dashboardPayments) ;
    }

    public MainDashboardResponseDto dashBoardToResponse (List<String> memberNames, List<DashboardQuestion> questions,
                                                         List<DashboardNotice> recentNotices, List<DashboardChatRoom> chatRooms,
                                                         List<DashboardPayment> dashboardPayments){
        MainDashboardResponseDto dto = new MainDashboardResponseDto();
        dto.setTodaySummary(buildTodaySummary());
        dto.setTodayMemberNames(memberNames);
        dto.setDashboardQuestions(questions);
        dto.setRecentNotices(recentNotices);
        dto.setDashboardChatRooms(chatRooms);
        dto.setDashboardPayments(dashboardPayments);
        dto.setDashboardWeeklies(dashboardWeekly());


        return dto;
    }

    private List<DailyCountDto> getWeeklyReportCounts() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        return fillMissingDates(
                reportService.weeklyReportCount(start.atStartOfDay(), today.atTime(LocalTime.MAX)),
                start,
                today
        );
    }


    //일일데이터 조회
    public TodaySummary buildTodaySummary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        int newMemberCount = memberService.todayNewMemberCount(todayStart, todayEnd);
        int reportCount = reportService.todayReportCount(todayStart, todayEnd);
        int activeChatRoomCount = chatRoomService.countActiveRooms(todayStart, todayEnd);
        int paymentCount = paymentService.todayPaymentCount(todayStart, todayEnd);

        return new TodaySummary(newMemberCount, reportCount, activeChatRoomCount, paymentCount);
    }

    //주간 집계 중 데이터 0으로 빠진 날짜 보완 (누락되지 않도록)
    public List<DailyCountDto> fillMissingDates(
            List<DailyCountDto> rawData,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // 날짜 별 집계 수 형태로 변환
        Map<LocalDate, Integer> dataMap = rawData.stream()
                .collect(Collectors.toMap(
                        DailyCountDto::getDate,
                        DailyCountDto::getCount
                ));

        //결과 답을 리스트 생성
        List<DailyCountDto> filled = new ArrayList<>();
        //오늘 기준으로 미래 여부 판단 (미래 데이터는 없으니 0 으로 처리)
        LocalDate today = LocalDate.now();

        //주간 시작일부터 종료일까지 순회하며 빈 날짜 채움
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            //해당 날짜의 count 값 (없으면 0으로 채움)
            int count = dataMap.getOrDefault(date, 0);
            //DailyCountDto 객체 생성하여 결과 리스트에 추가
            filled.add(new DailyCountDto(date, count));
        }

        return filled;
    }

    //주간 데이터 적용
    public DashboardWeekly dashboardWeekly() {
        // 기준 날짜
        LocalDate today = LocalDate.now();

        // 오늘 기준 과거 6일 ~ 오늘 (총 7일)
        LocalDate startDate = today.minusDays(6);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(LocalTime.MAX);  // 23:59:59.999999999

        //주간 신규 가입자 수
        List<DailyCountDto> memberCountRaw = memberService.weeklyNewMember(startDateTime, endDateTime);
        //주간 진행중인 채팅방 수
        List<DailyCountDto> chatRoomCountRaw = chatRoomService.weeklyActiveChatRoom(startDateTime, endDateTime);
        //주간 신고건 수
        List<DailyCountDto> reportCountRaw = reportService.weeklyReportCount(startDateTime, endDateTime);
        //주간 결제건 수
        List<DailyCountDto> paymentCountRaw = paymentService.weeklyPaymentCount(startDateTime, endDateTime);

        // 데이터 없는 날짜에 0 적용
        List<DailyCountDto> memberCount = fillMissingDates(memberCountRaw, startDate, today);
        List<DailyCountDto> chatRoomCount = fillMissingDates(chatRoomCountRaw, startDate, today);
        List<DailyCountDto> reportCount = fillMissingDates(reportCountRaw, startDate, today);
        List<DailyCountDto> paymentCount = fillMissingDates(paymentCountRaw, startDate, today);

        //추가
       return new DashboardWeekly(startDate, today, memberCount, chatRoomCount, reportCount, paymentCount);

    }
}
