package com.example.dice_talk.dashboard.dto;

import com.example.dice_talk.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(name = "DashboardNoticeDto", description = "대시보드(공지) DTO")
@Getter
@AllArgsConstructor
public class DashboardNotice {
    //최근 등록된 notice
    @Schema(description = "최근 등록된 공지글(이벤트)", example = "[점검 안내] Dice Talk 서비스 정기 점검 안내")
    private String recentNoticeTitle;
    //진행중인 이벤트
    @Schema(description = "진행중인 이벤트 수", example = "2")
    private int activeEventCount;
}

