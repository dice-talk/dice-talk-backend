package com.example.dice_talk.theme.dto;

import com.example.dice_talk.theme.entity.Theme;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

public class ThemeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @NotBlank(message = "테마명은 필수 입력란입니다.")
        private String name;

        @NotBlank(message = "테마 설명은 필수 입력란입니다.")
        private String description;

        private String image;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patch{
        private long themeId;

        private String name;

        private String description;

        private String image;

        private Theme.ThemeStatus themeStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private long themeId;
        private String name;
        private String description;
        private String image;
        private Theme.ThemeStatus themeStatus;
    }
}
