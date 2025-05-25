package com.example.dice_talk.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatReportDto {

    @Schema(name = "ChatReportPostDto", description = "채팅-신고 관계 생성 요청 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @Schema(description = "채팅 ID", example = "101")
        private long chatId;
    }

    @Schema(name = "ChatReportResponseDto", description = "채팅-신고 관계 응답 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        @Schema(description = "채팅신고 ID", example = "1")
        private long chatReportId;

        @Schema(description = "신고 ID", example = "20")
        private long reportId;

        @Schema(description = "채팅 ID", example = "101")
        private long chatId;
    }
}
