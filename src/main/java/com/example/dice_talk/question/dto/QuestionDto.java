package com.example.dice_talk.question.dto;

import com.example.dice_talk.answer.dto.AnswerDto;
import com.example.dice_talk.question.entity.Question;
import com.example.dice_talk.validator.NotSpace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class QuestionDto {
    @Schema(name = "QuestionPostDto", description = "질문 생성 DTO")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class Post{
        @Schema(description = "질문 제목", example = "다이스톡 언제 오픈하나요?")
        @NotBlank(message = "제목은 필수 입력란입니다.")
        private String title;

        @Schema(description = "질문 내용", example = "언제 오픈하나요?")
        @NotBlank(message = "내용은 필수 입력란입니다.")
        private String content;

        @Schema(description = "회원 ID", example = "2")
        private Long memberId;
    }

    @Schema(name = "GuestQuestionPostDto", description = "비회원 질문 생성 DTO")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class GuestPost{
        @Schema(description = "질문 제목", example = "다이스톡 언제 오픈하나요?")
        @NotBlank(message = "제목은 필수 입력란입니다.")
        private String title;

        @Schema(description = "질문 내용", example = "언제 오픈하나요?")
        @NotBlank(message = "내용은 필수 입력란입니다.")
        private String content;

        @Schema(description = "인증된 이메일", example = "2")
        private String email;
    }

    @Schema(name = "QuestionPatchDto", description = "질문 수정 DTO")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class Patch{
        @Schema(description = "질문 ID", example = "2")
        private long questionId;

        @Schema(description = "수정할 질문 제목", example = "수정된 질문 제목입니다.")
        @NotSpace
        private String title;

        @Schema(description = "수정할 질문 내용", example = "수정된 질문 내용입니다.")
        @NotSpace
        private String content;

        @Schema(description = "회원 ID", example = "2")
        private long memberId;

        @Schema(description = "질문 상태", example = "QUESTION_REGISTERED")
        private Question.QuestionStatus questionStatus;

        @Schema(description = "유지할 이미지 ID 목록, example = \"[101,102]\"")
        private List<Long> keepImageIds;
    }

    @Schema(name = "QuestionResponseDto", description = "질문 응답 DTO")
    @Getter
    @AllArgsConstructor
    public static class Response{
        @Schema(description = "질문 ID", example = "2")
        private Long questionId;

        @Schema(description = "질문 제목", example = "다이스톡은 언제 오픈하나요?")
        private String title;

        @Schema(description = "질문 내용", example = "언제 오픈하나요?")
        private String content;

        @Schema(description = "질문 상태", example = "QUESTION_REGISTERED")
        private Question.QuestionStatus questionStatus;

        @Schema(description = "회원 ID", example = "2")
        private Long memberId;

        @Schema(description = "작성자 Email", example = "user@gmail.com")
        private String email;

        @Schema(description = "답변 Response DTO", implementation = AnswerDto.Response.class)
        private AnswerDto.Response answer;

        @Schema(description = "질문 이미지 DTO 목록", implementation = QuestionImageDto.Response.class)
        private List<QuestionImageDto.Response> questionImages;

        @Schema(description = "등록 시간", example = "2025-05-18T11:35:00")
        private LocalDateTime createdAt;

        @Schema(description = "최종 수정 시간", example = "2025-05-18T11:35:00")
        private LocalDateTime modifiedAt;
    }
}
