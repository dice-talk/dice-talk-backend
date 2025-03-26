package com.example.dice_talk.product.dto;

import com.example.dice_talk.validator.NotSpace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ProductDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @NotBlank(message = "상품의 이름은 필수 입력란입니다.")
        private String productName;

//        @NotBlank(message = "상품의 가격은 필수 입력란입니다.")
        private int price;

//        @NotBlank(message = "상품의 수량은 필수 입력란입니다.")
        private int quantity;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{
        private long productId;

//        @NotSpace
        private String productName;

//        @NotSpace
        private int price;

//        @NotSpace
        private int quantity;
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private long productId;
        private String productName;
        private int price;
        private int quantity;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
