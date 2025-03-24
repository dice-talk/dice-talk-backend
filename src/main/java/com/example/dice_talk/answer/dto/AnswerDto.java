package com.example.dice_talk.answer.dto;

import com.example.dice_talk.validator.NotSpace;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

public class AnswerDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Post{
        @NotBlank(message = "답변 내용은 필수입니다.")
        private String content;

        private String answerImage;

        private Long questionId;

        private Long memberId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Patch{
        private Long answerId;

        @NotSpace(message = "답변 내용은 필수입니다.")
        private String content;

        private String answerImage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response{
        private Long answerId;
        private String content;
        private String answerImage;
        private Long questionId;
        private Long memberId;
    }
}
