package com.example.dice_talk.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Schema(name = "DashboardWeekly", description = "대시보드(주간) DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardWeekly {

    @Schema(description = "주간 시작 날짜", example = "2025-05-20")
    private LocalDate weekStartDate;

    @Schema(description = "주간 종료 날짜", example = "2025-05-26")
    private LocalDate weekEndDate;

    //주간 가입자 수
    @Schema(
            description = "주간 가입자 수",
            example = "[{\"date\": \"2025-05-25\", \"count\": 11}]"
    )    private List<DailyCountDto> weeklyNewMemberCount;
    //주간 진행중인 채팅방 수
    @Schema(
            description = "주간 진행된 채팅방 수",
            example = "[{\"date\": \"2025-05-25\", \"count\": 7}]"
    )
    private List<DailyCountDto> weeklyActiveChatRoomCount;

    //주간 신고건 수
    @Schema(
            description = "주간 접수된 신고 수",
            example = "[{\"date\": \"2025-05-25\", \"count\": 4}]"
    )
    private List<DailyCountDto> weeklyReportCount;
    //주간 결제건 수
    @Schema(
            description = "주간 진행된 결제 수",
            example = "[{\"date\": \"2025-05-25\", \"count\": 8}]"
    )
    private List<DailyCountDto> weeklyPaymentCount;
}
