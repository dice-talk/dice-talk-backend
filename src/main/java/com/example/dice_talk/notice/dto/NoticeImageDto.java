package com.example.dice_talk.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class NoticeImageDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        private Long noticeId;
        private String imageUrl;
        private boolean isThumbnail;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{
        private Long noticeImageId;
        private Long noticeId;
        private String imageUrl;
        private boolean isThumbnail;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private Long noticeImageId;
        private Long noticeId;
        private String imageUrl;
        private boolean isThumbnail;
    }
}
