package com.example.dice_talk.event.dto;

import com.example.dice_talk.event.entity.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Schema(name = "EventDto", description = "이벤트 DTO")
public class EventDto {

    @Schema(name = "EventPostDto", description = "이벤트 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @Schema(description = "이벤트 이름", example = "하트 보내기 이벤트")
        @NotBlank(message = "이벤트명은 필수 입력란입니다.")
        private String eventName;

        @Schema(description = "테마 ID", example = "1")
        private long themeId;
    }

    @Schema(name = "EventPatchDto", description = "이벤트 수정 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patch{
        @Schema(description = "이벤트 ID", example = "1")
        private long eventId;

        @Schema(description = "이벤트 이름", example = "하트 보내기 이벤트")
        private String eventName;

        @Schema(description = "이벤트 상태", example = "EVENT_OPEN")
        private Event.EventStatus eventStatus;

        @Schema(description = "테마 ID", example = "1")
        private long themeId;
    }

    @Schema(name = "EventResponseDto", description = "이벤트 응답 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        @Schema(description = "이벤트 ID", example = "1")
        private long eventId;

        @Schema(description = "이벤트 이름", example = "사랑의 작대기")
        private String eventName;

        @Schema(description = "이벤트 상태", example = "EVENT_OPEN")
        private Event.EventStatus eventStatus;

        @Schema(description = "테마 ID", example = "1")
        private long themeId;
    }
}
