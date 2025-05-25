package com.example.dice_talk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;


@Schema(name = "MultiResponseDto", description = "복수 데이터 응답 DTO")
@Getter
public class MultiResponseDto<T> {
    @Schema(description = "복수 데이터")
    private List<T> data;

    @Schema(description = "페이지네이션 정보")
    private PageInfo pageInfo;

    public MultiResponseDto(List<T> data, Page page) {
        this.data = data;
        this.pageInfo = new PageInfo(page.getNumber() + 1,
                page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
