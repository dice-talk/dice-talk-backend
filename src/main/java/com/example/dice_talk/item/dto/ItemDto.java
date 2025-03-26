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

    @Getter
    @AllArgsConstructor
    public static class Response{
        private long itemId;
        private String itemName;
        private String description;
        private int dicePrice;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
