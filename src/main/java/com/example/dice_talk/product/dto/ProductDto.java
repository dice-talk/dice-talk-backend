package com.example.dice_talk.product.dto;

import com.example.dice_talk.validator.NotSpace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ProductDto {
    @Schema(description = "상품 생성 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        @Schema(description = "상품 이름", example = "다이스 10개")
        @NotBlank(message = "상품의 이름은 필수 입력란입니다.")
        private String productName;

        @Schema(description = "상품 가격", example = "1300")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        private int price;

        @Schema(description = "다이스 충전 수량", example = "10")
        @Min(value = 0, message = "수량은 0 이상이어야 합니다.")
        private int quantity;
    }

    @Schema(description = "상품 수정 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{
        @Schema(description = "상품 ID", example = "1")
        private long productId;

        @Schema(description = "수정할 상품 이름", example = "다이스 10 + 1개")
        private String productName;

        @Schema(description = "상품 이미지 URL", example = "https://s3.amazonaws.com/bucket/image.png")
        private String productImage;

        @Schema(description = "수정할 상품 가격", example = "1300")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        private int price;

        @Schema(description = "수정할 다이스 충전 수량", example = "11")
        @Min(value = 0, message = "수량은 0 이상이어야 합니다.")
        private int quantity;
    }

    @Schema(description = "상품 응답 DTO")
    @Getter
    @AllArgsConstructor
    public static class Response{
        @Schema(description = "상품 ID", example = "1")
        private long productId;

        @Schema(description = "상품 이름", example = "다이스 10개")
        private String productName;

        @Schema(description = "상품 이미지 URL", example = "https://s3.amazonaws.com/bucket/image.png")
        private String productImage;

        @Schema(description = "상품 가격", example = "1300")
        private int price;

        @Schema(description = "다이스 충전 수량", example = "10")
        private int quantity;

        @Schema(description = "등록 시간", example = "2025-05-24T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정 시간", example = "2025-05-24T12:00:00")
        private LocalDateTime modifiedAt;
    }
}
