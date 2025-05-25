package com.example.dice_talk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(name = "SingleResponse", description = "단일 데이터 DTO")
@AllArgsConstructor
@Getter
public class SingleResponseDto<T> {

    @Schema(description = "응답 데이터", implementation = Object.class)
    private T data;
}
