package com.example.dice_talk.dashboard.service;

import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.dashboard.dto.*;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notice.service.NoticeService;
import com.example.dice_talk.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final MemberService memberService;
    private final QuestionService questionService;
    private final NoticeService noticeService;
    private final ChatRoomService chatRoomService;

    public MainDashboardResponseDto findVerifiedExistsDashBoard() {
        //회원 관리
        //오늘 회원가입한 회원의 이름
        List<String> newMemberNames = memberService.findTodayRegisteredMembers();

        //문의글 관리 : 조회일 기준 최근 7일간 등록된 질문 수 + 미답변 질문 수
        List<DashboardQuestion> dashboardQuestions = questionService.findUnansweredQuestions();

        //공지+이벤트 관리 = 최근 등록된 공지 + 진행중인 이벤트
        List<DashboardNotice> dashboardNotices = noticeService.findRecentNotices();

        //채팅방 관리 : 잔행중인 채팅방(단체 /1대1)
        List<DashboardChatRoom> chatRooms = chatRoomService.activeChatRoomCount();

        //신고 관리 : 최근 7일간

        //결제 관리

        //주간 데이터


        return dashBoardToResponse(newMemberNames, dashboardQuestions, dashboardNotices, chatRooms) ;
    }

    public MainDashboardResponseDto dashBoardToResponse (List<String> memberNames, List<DashboardQuestion> questions,
                                                         List<DashboardNotice> recentNotices, List<DashboardChatRoom> chatRooms){
        MainDashboardResponseDto dto = new MainDashboardResponseDto();
        dto.setTodayMemberCount(memberNames.size());
        dto.setTodayMemberNames(memberNames);
        dto.setDashboardQuestions(questions);
        dto.setRecentNotices(recentNotices);
        dto.setDashboardChatRooms(chatRooms);
        dto.setDashboardWeeklies(dashboardWeekly());

        return dto;
    }

    //주간 데이터 적용
    public List<DashboardWeekly> dashboardWeekly () {
        //주간 구하기
        LocalDate today = LocalDate.now();
        //today를 기준으로 같은 주의 월요일 날짜를 구함
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));  //오늘과 같거나 그 이전 중에서 가까운 월요일로 이동
        LocalDateTime weekStart = monday.atStartOfDay();     //이번주 월요일 00:00
        LocalDateTime dayAfter = today.plusDays(1).atStartOfDay();  //오늘 기준 다음날 00:00

        //주간 신규 가입자 수
        List<DailyCountDto> memberCount = memberService.weeklyNewMember(weekStart, dayAfter);
        //주간 진행중인 채팅방 수
        List<DailyCountDto> chatRoomCount = chatRoomService.weeklyActiveChatRoom(weekStart, dayAfter);
        //주간 신고건 수
        //주간 결제건 수

        //추가
        List<DashboardWeekly> dashboardWeeklies = new ArrayList<>();
        dashboardWeeklies.add(new DashboardWeekly(memberCount, chatRoomCount));

       return dashboardWeeklies;
    }
}
