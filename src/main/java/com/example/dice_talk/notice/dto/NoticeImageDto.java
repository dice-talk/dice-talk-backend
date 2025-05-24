package com.example.dice_talk.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class NoticeImageDto {

    @Schema(description = "공지 이미지 생성 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        @Schema(description = "공지/이벤트 ID", example = "1")
        private Long noticeId;

        @Schema(description = "이미지 URL", example = "https://s3.amazonaws.com/bucket/notice1.png")
        private String imageUrl;

        @Schema(description = "썸네일 여부", example = "true")
        private boolean isThumbnail;
    }

    @Schema(description = "공지 이미지 수정 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{
        @Schema(description = "공지 이미지 ID", example = "10")
        private Long noticeImageId;

        @Schema(description = "공지/이벤트 ID", example = "1")
        private Long noticeId;

        @Schema(description = "이미지 URL", example = "https://s3.amazonaws.com/bucket/notice1_updated.png")
        private String imageUrl;

        @Schema(description = "썸네일 여부", example = "false")
        private boolean isThumbnail;
    }

    @Schema(description = "공지 이미지 응답 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        @Schema(description = "공지 이미지 ID", example = "10")
        private Long noticeImageId;

        @Schema(description = "공지/이벤트 ID", example = "1")
        private Long noticeId;

        @Schema(description = "이미지 URL", example = "https://s3.amazonaws.com/bucket/notice1.png")
        private String imageUrl;

        @Schema(description = "썸네일 여부", example = "true")
        private boolean isThumbnail;
    }
}
