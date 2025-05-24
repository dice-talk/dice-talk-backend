package com.example.dice_talk.report.dto;

import com.example.dice_talk.chat.dto.ChatDto;
import com.example.dice_talk.report.entity.ChatReport;
import com.example.dice_talk.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class ReportDto {

    @Schema(description = "신고 생성 요청 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        @Schema(description = "신고 사유", example = "부적절한 채팅 내용")
        @NotBlank(message = "신고 사유는 필수 입력 항목입니다.")
        private String reason;

        @Schema(description = "신고자 회원 ID", example = "123")
        private long reporterId;

        @Schema(description = "채팅 신고 목록 DTO", implementation = ChatReportDto.Post.class)
        private List<ChatReportDto.Post> chatReports;

        @Schema(description = "신고 대상 회원 ID 목록", example = "[456,789]")
        private List<Long> reportedMemberIds;
    }

    @Schema(description = "신고 응답 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        @Schema(description = "신고 ID", example = "1")
        private long reportId;

        @Schema(description = "신고 사유", example = "부적절한 채팅 내용")
        private String reason;

        @Schema(description = "신고자 회원 ID", example = "123")
        private long reporterId;

        @Schema(description = "신고된 채팅 목록", implementation = ChatDto.Response.class)
        private List<ChatDto.Response> reportedChats;

        @Schema(description = "신고 처리 상태", example = "REPORT_RECEIVED")
        private Report.ReportStatus reportStatus;
    }
}
