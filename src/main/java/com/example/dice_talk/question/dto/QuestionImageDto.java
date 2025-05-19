package com.example.dice_talk.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class QuestionImageDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        @Schema(description = "질문 ID", example = "2")
        private Long questionId;

        @Schema(description = "이미지 주소", example = "https://~")
        private String imageUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        @Schema(description = "질문 이미지 ID", example = "2")
        private Long questionImageId;

        @Schema(description = "질문 ID", example = "2")
        private Long questionId;

        @Schema(description = "이미지 주소", example = "https://~")
        private String imageUrl;
    }
}
