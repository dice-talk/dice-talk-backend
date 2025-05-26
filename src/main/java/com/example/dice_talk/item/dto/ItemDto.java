package com.example.dice_talk.item.dto;

import com.example.dice_talk.validator.NotSpace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ItemDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{

        @NotBlank(message = "아이템의 이름은 필수 입력란입니다.")
        private String itemName;

        @NotBlank(message = "아이템 설명은 필수 입력란입니다.")
        private String description;

        private int dicePrice;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{
        private long itemId;

        @NotSpace
        private String itemName;

        @NotSpace
        private String description;

        private int dicePrice;
    }

    @Schema(name = "ItemResponseDto", description = "아이템 반환 Dto")
    @Getter
    @AllArgsConstructor
    public static class Response{
        @Schema(description = "아이템 ID", example = "1")
        private long itemId;
        @Schema(description = "아이템 이름", example = "채팅방 나가기")
        private String itemName;
        @Schema(description = "아이템 설명", example = "하루 채팅방 나가기 2회 시 아이템을 사용해야 합니다.")
        private String description;
        @Schema(description = "아이템 이미지 파일 주소", example = "url://dicetalk.image")
        private String itemImage;
        @Schema(description = "아이템 사용 금액", example = "900")
        private int dicePrice;
        @Schema(description = "등록 시간", example = "2025-05-01 13:43:22")
        private LocalDateTime createdAt;
        @Schema(description = "수정 시간", example = "2025-05-01 13:43:22")
        private LocalDateTime modifiedAt;
    }
}
