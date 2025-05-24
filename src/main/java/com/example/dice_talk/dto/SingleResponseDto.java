package com.example.dice_talk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SingleResponseDto<T> {

    @Schema(description = "응답 데이터 리스트")
    private T data;
}
