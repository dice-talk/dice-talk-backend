package com.example.dice_talk.notice.dto;

import com.example.dice_talk.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;


public class NoticeDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post {
        @NotBlank(message = "공지사항/이벤트의 제목은 필수 입력란입니다.")
        private String title;

        @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
        private String content;

        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Notice.NoticeType noticeType;
        private Notice.NoticeStatus noticeStatus;
        private int noticeImportance;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch {
        private long noticeId;

        @NotBlank(message = "공지사항/이벤트의 제목은 필수 입력란입니다.")
        private String title;

        @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
        private String content;

        private List<Long> keepImageIds;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Notice.NoticeType noticeType;
        private Notice.NoticeStatus noticeStatus;
        private int noticeImportance;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private long noticeId;
        private String title;
        private String content;
        private List<NoticeImageDto.Response> noticeImages;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Notice.NoticeType noticeType;
        private Notice.NoticeStatus noticeStatus;
        private int noticeImportance;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
