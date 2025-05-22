package com.example.dice_talk.dashboard.service;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.dashboard.dto.DashboardChatRoom;
import com.example.dice_talk.dashboard.dto.DashboardNotice;
import com.example.dice_talk.dashboard.dto.DashboardQuestion;
import com.example.dice_talk.dashboard.dto.MainDashboardResponseDto;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notice.service.NoticeService;
import com.example.dice_talk.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        //문의글 관리 :
        List<DashboardQuestion> dashboardQuestions = questionService.findUnansweredQuestions();

        //공지+이벤트 관리 = 최근 등록된 공지 + 진행중인 이벤트
        List<DashboardNotice> dashboardNotices = noticeService.findRecentNotices();

        //채팅방 관리 : 잔행중인 채팅방(단체 /1대1)
        List<DashboardChatRoom> chatRooms = chatRoomService.activeChatRoomCount();

        //신고 관리 : 최근 7일간

        //결제 관리




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

        return dto;
    }
}
