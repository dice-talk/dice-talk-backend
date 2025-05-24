package com.example.dice_talk.notice.dto;

import com.example.dice_talk.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;


public class NoticeDto {

    @Schema(description = "공지/이벤트 등록 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post {
        @Schema(description = "제목", example = "서비스 점검 안내")
        @NotBlank(message = "공지사항/이벤트의 제목은 필수 입력란입니다.")
        private String title;

        @Schema(description = "내용", example = "이번 주 금요일 00시부터 02시까지 서비스 점검이 진행됩니다.")
        @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
        private String content;

        @Schema(description = "시작 일시", example = "2025-06-01T00:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2025-06-02T00:00:00")
        private LocalDateTime endDate;

        @Schema(description = "공지 유형", example = "NOTICE")
        private Notice.NoticeType noticeType;

        @Schema(description = "공지 상태", example = "PUBLISHED")
        private Notice.NoticeStatus noticeStatus;

        @Schema(description = "공지 중요도", example = "1")
        private int noticeImportance;
    }

    @Schema(description = "공지/이벤트 수정 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch {
        @Schema(description = "공지/이벤트 ID", example = "1")
        private long noticeId;

        @Schema(description = "제목", example = "서비스 점검 일정 변경 안내")
        @NotBlank(message = "공지사항/이벤트의 제목은 필수 입력란입니다.")
        private String title;

        @Schema(description = "내용", example = "점검 시간이 01시부터로 변경되었습니다.")
        @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
        private String content;

        @Schema(description = "유지할 이미지 ID 목록", example = "[101,102]")
        private List<Long> keepImageIds;

        @Schema(description = "시작 일시", example = "2025-06-01T00:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2025-06-02T00:00:00")
        private LocalDateTime endDate;

        @Schema(description = "공지 유형", example = "EVENT")
        private Notice.NoticeType noticeType;

        @Schema(description = "공지 상태", example = "DRAFT")
        private Notice.NoticeStatus noticeStatus;

        @Schema(description = "공지 중요도", example = "2")
        private int noticeImportance;
    }

    @Schema(description = "공지/이벤트 응답 DTO")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @Schema(description = "공지/이벤트 ID", example = "1")
        private long noticeId;

        @Schema(description = "제목", example = "서비스 점검 안내")
        private String title;

        @Schema(description = "내용", example = "이번 주 금요일 00시부터 02시까지 서비스 점검이 진행됩니다.")
        private String content;

        @Schema(description = "공지 이미지 DTO 목록", implementation = NoticeImageDto.Response.class)
        private List<NoticeImageDto.Response> noticeImages;

        @Schema(description = "시작 일시", example = "2025-06-01T00:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2025-06-02T00:00:00")
        private LocalDateTime endDate;

        @Schema(description = "공지 유형", example = "NOTICE")
        private Notice.NoticeType noticeType;

        @Schema(description = "공지 상태", example = "PUBLISHED")
        private Notice.NoticeStatus noticeStatus;

        @Schema(description = "공지 중요도", example = "1")
        private int noticeImportance;

        @Schema(description = "등록 시간", example = "2025-05-24T14:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정 시간", example = "2025-05-24T15:30:00")
        private LocalDateTime modifiedAt;
    }

}
