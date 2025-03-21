package com.example.dice_talk.item.dto;

import com.example.dice_talk.validator.NotSpace;
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
        @NotBlank(message = "상품의 이름은 필수 입력란입니다.")
        private String itemName;

        @NotBlank(message = "다이스 수량은 필수 입력란입니다.")
        private int quantity;

        @NotBlank(message = "상품의 가격은 필수 입력란입니다.")
        private int price;
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
        private int quantity;

        @NotSpace
        private int price;
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private long itemId;
        private String itemName;
        private int quantity;
        private int price;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
