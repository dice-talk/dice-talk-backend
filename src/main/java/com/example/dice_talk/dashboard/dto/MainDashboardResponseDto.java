package com.example.dice_talk.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(name = "MainDashboardResponseDto", description = "메인화면 대시보드 응답 DTO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MainDashboardResponseDto {
    //회원관리
    @Schema(description = "오늘 가입한 회원 수", example = "20")
    private int todayMemberCount;

    @Schema(description = "오늘 가입한 회원 명단", example = "[{\"name\": \"기로기\"}]")
    private List<String> todayMemberNames;

    //QnA 관리
    @Schema(description = "답변이 없는 질문글 개수", example = "20")
    private int unansweredQuestionCount;

    @Schema(description = "미답변 질문글 & 주간 등록된 질문글 수와 제목", example = "[{\"title\": \"회원가입 버튼 클릭이 안됩니다.\"}]")
    private List<DashboardQuestion> dashboardQuestions;

    //notice 관리
    @Schema(description = "최근 공지 리스트",
            example = "[{\"title\": \"점검 안내.\", \"createdAt\": \"2025-04-11T11:30\"}]")
    private List<DashboardNotice> recentNotices;

    //채팅방 관리
    @Schema(description = "진행중인 채팅방 수", example = "[{\"activeChatRoom\": 40, \"activeGroupChatRoom\": 33, \"activeCoupleChatRoom\": 7}]")
    private List<DashboardChatRoom> dashboardChatRooms;

    //주간 데이터
    @Schema(
            description = "주간 데이터",
            example = "[" +
                    "{" +
                    "\"weekStartDate\": \"2025-05-20\"," +
                    "\"weekEndDate\": \"2025-05-26\"," +
                    "\"weeklyNewMemberCount\": [" +
                    "{\"date\": \"2025-05-20\", \"count\": 5}," +
                    "{\"date\": \"2025-05-21\", \"count\": 7}" +
                    "]," +
                    "\"weeklyActiveChatRoomCount\": [" +
                    "{\"date\": \"2025-05-20\", \"count\": 4}," +
                    "{\"date\": \"2025-05-21\", \"count\": 6}" +
                    "]" +
                    "}" +
                    "]"
    )
    private List<DashboardWeekly> dashboardWeeklies;
}
