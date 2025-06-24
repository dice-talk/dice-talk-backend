package com.example.dice_talk.dashboard.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(name = "DashboardPayment", description = "대시보드 결제 요약 정보 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPayment {

    @Schema(description = "오늘 다이스 충전 총 금액", example = "35000")
    private int todayTotalAmount;

    @Schema(description = "이번 달 다이스 충전 총 금액", example = "520000")
    private int monthlyTotalAmount;

    @Schema(description = "금일 아이템 사용 건수", example = "18")
    private int todayItemUsageCount;

    @Schema(description = "가장 많이 결제한 사용자 TOP 3",
            example = "[{\"memberId\": 1, \"email\": \"top1@example.com\", \"totalAmount\": 150000}]")
    private List<TopPayerDto> topPayers;
}