package com.example.dice_talk.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodaySummary {
    @Schema(description = "오늘 가입자 수", example = "5")
    private int newMemberCount;

    @Schema(description = "오늘 신고 건수", example = "2")
    private int reportCount;

    @Schema(description = "오늘 활성 채팅방 수", example = "12")
    private int activeChatRoomCount;

    @Schema(description = "오늘 결제 건수", example = "6")
    private int paymentCount;


}
