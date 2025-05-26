package com.example.dice_talk.answer.dto;

import com.example.dice_talk.validator.NotSpace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "AnswerDto", description = "답변 DTO")
public class AnswerDto {
    @Schema(name = "AnswerPostDto", description = "답변 등록 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Post {
        @Schema(description = "답변 내용", example = "네, 맞습니다.")
        @NotBlank(message = "답변 내용은 필수입니다.")
        private String content;

        @Schema(description = "질문 ID", example = "1", hidden = true)
        private Long questionId;

        @Schema(description = "회원 ID", example = "3", hidden = true)
        private Long memberId;
    }

    @Schema(name = "AnswerPatchDto", description = "답변 수정 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Patch {
        @Schema(description = "답변 ID", example = "10")
        private Long answerId;

        @Schema(description = "수정할 답변 내용", example = "수정된 답변입니다.")
        @NotSpace(message = "답변 내용은 필수입니다.")
        private String content;

        @Schema(description = "유지할 이미지 ID 목록")
        private List<Long> keepImageIds;
    }

    @Schema(name = "AnswerResponseDto", description = "답변 응답 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        @Schema(description = "답변 ID", example = "10")
        private Long answerId;

        @Schema(description = "답변 내용", example = "최종 답변입니다.")
        private String content;

        @Schema(description = "답변 이미지 DTO 목록")
        private List<AnswerImageDto.Response> answerImages;

        @Schema(description = "질문 ID", example = "1")
        private Long questionId;

        @Schema(description = "회원 ID", example = "3")
        private Long memberId;

        @Schema(description = "등록 시간", example = "2025-05-18T11:35:00")
        private LocalDateTime createdAt;

        @Schema(description = "최종 수정 시간", example = "2025-05-18T11:35:00")
        private LocalDateTime modifiedAt;
    }
}

