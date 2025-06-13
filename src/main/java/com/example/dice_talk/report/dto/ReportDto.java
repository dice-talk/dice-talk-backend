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
import java.time.LocalDateTime;
import java.util.List;

public class ReportDto {

    @Schema(name = "ReportPostDto", description = "신고 생성 요청 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post {
        @Schema(description = "신고 사유", example = "SPAM")
        private Report.ReportReason reportReason;

        @Schema(description = "신고자 회원 ID", example = "123")
        private long reporterId;

        @Schema(description = "채팅 신고 목록 DTO", implementation = ChatReportDto.Post.class)
        private List<ChatReportDto.Post> chatReports;

        @Schema(description = "신고 대상 회원 ID 목록", example = "[456,789]")
        private List<Long> reportedMemberIds;
    }

    @Schema(name = "ReportResponseDto", description = "신고 응답 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        @Schema(description = "신고 ID", example = "1")
        private long reportId;

        @Schema(description = "신고 사유", example = "SPAM")
        private String reportReason;

        @Schema(description = "신고자 회원 ID", example = "123")
        private Long reporterId;

        @Schema(description = "신고자 회원 이메일", example = "user1@gmail.com")
        private String reporterEmail;

        @Schema(description = "피신고자 회원 ID", example = "456")
        private Long reportedMemberId;

        @Schema(description = "피신고자 회원 이메일", example = "user2@gmail.com")
        private String reportedEmail;

        @Schema(description = "신고된 채팅 목록", implementation = ChatDto.Response.class)
        private List<ChatDto.Response> reportedChats;

        @Schema(description = "신고된 채팅의 채팅방 ID", example = "1")
        private Long chatRoomId;

        @Schema(description = "신고 처리 상태", example = "REPORT_RECEIVED")
        private String reportStatus;

        @Schema(description = "신고 접수 일시", example = "2025-05-28T00:00")
        private LocalDateTime createdAt;

        @Schema(description = "신고 처리 일시", example = "2025-05-29T12:00")
        private LocalDateTime modifiedAt;

        public static Response from(Report report, String reporterEmail, String reportedEmail) {
            Response response = new Response();
            response.setReportId(report.getReportId());
            response.setReportReason(report.getReportReason().getDescription());
            response.setReporterId(report.getReporterId());
            response.setReporterEmail(reporterEmail);
            response.setReportedMemberId(report.getReportedMemberId());
            response.setReportedEmail(reportedEmail);
            response.setReportStatus(report.getReportStatus().getStatus());
            response.setCreatedAt(report.getCreatedAt());
            response.setModifiedAt(report.getModifiedAt());
            return response;
        }
    }
}
