package com.example.dice_talk.report.dto;

import com.example.dice_talk.chat.dto.ChatDto;
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

        private List<Long> reportedMemberIds;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private long reportId;
        private String reason;
        private long reporterId;
        private List<ChatDto.Response> reportedChats;
        private Report.ReportStatus reportStatus;
    }
}
