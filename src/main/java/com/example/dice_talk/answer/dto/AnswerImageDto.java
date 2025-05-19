package com.example.dice_talk.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AnswerImageDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        @Schema(description = "답변 ID", example = "10")
        private Long answerId;

        @Schema(description = "이미지 주소", example = "https://~")
        private String imageUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        @Schema(description = "답변 이미지 ID", example = "1")
        private Long answerImageId;

        @Schema(description = "답변 ID", example = "2")
        private Long answerId;

        @Schema(description = "이미지 주소", example = "https://~")
        private String imageUrl;
    }
}
