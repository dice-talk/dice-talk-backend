package com.example.dice_talk.dashboard.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Schema(name = "DailyCountDto", description = "일일 집계 DTO")
@Getter
@Setter
@NoArgsConstructor
public class DailyCountDto {
    @Schema(description = "조회 날짜", example = "2025-05-25")
    private LocalDate date;

    @Schema(description = "날짜 별 집계 수", example = "11")
    private Long count;

    @QueryProjection
    public DailyCountDto(LocalDate date, Long count) {
        this.date = date;
        this.count = count;
    }
}
