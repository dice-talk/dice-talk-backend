package com.example.dice_talk.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(name = "DashboardWeekly", description = "대시보드(주간) DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardWeekly {
    //주간 가입자 수
    @Schema(description = "주간 가입자 수", example = "35")
    private List<DailyCountDto> weeklyNewMemberCount;
    //주간 진행중인 채팅방 수
    @Schema(description = "주간 진행된 채팅방 수", example = "123")
    private List<DailyCountDto> weeklyActiveChatRoomCount;
    //주간 신고건 수
//    private int weeklyReportCount;
    //주간 결제건 수
//    private int weeklyPaymentCount;
}
