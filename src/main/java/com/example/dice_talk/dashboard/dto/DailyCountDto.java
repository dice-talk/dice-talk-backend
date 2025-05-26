package com.example.dice_talk.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(name = "DailyCountDto", description = "일일 집계 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyCountDto {
    @Schema(description = "조회 날짜", example = "2025-05-25T00:00")
    LocalDateTime date;

    @Schema(description = "날짜 별 집계 수", example = "11")
    int count;
}
