package com.example.dice_talk.answer.dto;

import com.example.dice_talk.validator.NotSpace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

public class AnswerDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Post {
        @Schema(description = "답변 내용", example = "네, 맞습니다.")
        @NotBlank(message = "답변 내용은 필수입니다.")
        private String content;

        @Schema(description = "답변 이미지 URL", example = "https://example.com/image.jpg")
        private String answerImage;

        @Schema(description = "질문 ID", example = "1", hidden = true)
        private Long questionId;

        @Schema(description = "회원 ID", example = "3", hidden = true)
        private Long memberId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Patch {
        @Schema(description = "답변 ID", example = "10")
        private Long answerId;

        @Schema(description = "수정할 답변 내용", example = "수정된 답변입니다.")
        @NotSpace(message = "답변 내용은 필수입니다.")
        private String content;

        @Schema(description = "답변 이미지 URL", example = "https://example.com/new-image.jpg")
        private String answerImage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        @Schema(description = "답변 ID", example = "10")
        private Long answerId;

        @Schema(description = "답변 내용", example = "최종 답변입니다.")
        private String content;

        @Schema(description = "답변 이미지 URL", example = "https://example.com/final.jpg")
        private String answerImage;

        @Schema(description = "질문 ID", example = "1")
        private Long questionId;

        @Schema(description = "회원 ID", example = "3")
        private Long memberId;
    }
}

