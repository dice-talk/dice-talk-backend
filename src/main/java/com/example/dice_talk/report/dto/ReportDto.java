package com.example.dice_talk.report.dto;

import com.example.dice_talk.report.entity.ChatReport;
import com.example.dice_talk.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class ReportDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        @NotBlank
        private String reason;

        private long reporterId;

        private List<ChatReportDto.Post> chatReports;
    }

//    @Getter
//    @Setter
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class Patch{
//        private long reportId;
//
//        private String reason;
//
//        private long reporterId;
//
//        private List<ChatReportDto> chatReports;
//
//        private Report.ReportStatus reportStatus = Report.ReportStatus.REPORT_RECEIVED;
//    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private long reportId;
        private String reason;
        private long reporterId;
        private List<ChatReportDto.Response> chatReports;
        private Report.ReportStatus reportStatus;
    }
}
