package com.example.dice_talk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Schema(name = "ListResponseDto", description = "복수 데이터 응답 DTO (페이지네이션 적용 x)")
@Getter
public class ListResponseDto<T> {
    @Schema(description = "복수 데이터")
    private List<T> data;

    public ListResponseDto(List<T> data) {
        this.data = data;
    }
}
