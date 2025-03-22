package com.example.dice_talk.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatReportDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        private long chatId;
    }

    public static class Response{
        private long chatReportId;
        private long reportId;
        private long chatId;
    }
}
