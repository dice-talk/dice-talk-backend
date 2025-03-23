package com.example.dice_talk.event.dto;

import com.example.dice_talk.event.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

public class EventDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @NotBlank(message = "이벤트명은 필수 입력란입니다.")
        private String eventName;

        private long themeId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patch{
        private long eventId;
        private String eventName;
        private Event.EventStatus eventStatus;
        private long themeId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private long eventId;
        private String eventName;
        private Event.EventStatus eventStatus;
        private long themeId;
    }
}
