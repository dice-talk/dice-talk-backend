package com.example.dice_talk.theme.dto;

import com.example.dice_talk.theme.entity.Theme;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Schema(description = "테마 DTO")
public class ThemeDto {

    @Schema(name = "ThemePostDto", description = "테마 생성 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @Schema(description = "테마명", example = "하트시그널")
        @NotBlank(message = "테마명은 필수 입력란입니다.")
        private String name;

        @Schema(description = "테마 설명", example = "하트시그널 테마입니다.")
        @NotBlank(message = "테마 설명은 필수 입력란입니다.")
        private String description;
    }

    @Schema(name = "ThemePatchDto", description = "테마 수정 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patch{
        @Schema(description = "테마 ID", example = "1")
        private long themeId;

        @Schema(description = "테마명", example = "나는 SOLO")
        private String name;

        @Schema(description = "테마 설명", example = "나는 SOLO 테마입니다.")
        private String description;

        @Schema(description = "테마 이미지 URL", example = "https://s3~")
        private String image;

        @Schema(description = "테마 상태", example = "THEME_ON")
        private Theme.ThemeStatus themeStatus;
    }

    @Schema(name = "ThemeResponseDto", description = "테마 응답 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        @Schema(description = "테마 ID", example = "1")
        private long themeId;

        @Schema(description = "테마명", example = "나는 SOLO")
        private String name;

        @Schema(description = "테마 설명", example = "나는 SOLO 테마입니다.")
        private String description;

        @Schema(description = "테마 이미지 URL", example = "https://s3~")
        private String image;

        @Schema(description = "테마 상태", example = "THEME_ON")
        private Theme.ThemeStatus themeStatus;
    }
}
