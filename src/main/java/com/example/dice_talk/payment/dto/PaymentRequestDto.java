package com.example.dice_talk.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Min;

@Schema(description = "결제 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    @Schema(description = "결제 금액", example = "10000", minimum = "1000")
    @Min(value = 1000, message = "최소 결제 금액은 1,000원입니다.")
    private int amount;         // 결제 금액

    @Schema(description = "다이스 양", example = "100", minimum = "1")
    @Min(value = 1, message = "최소 1개의 다이스를 구매해야 합니다.")
    private int diceAmount;     // 다이스 양

    @Schema(description = "구매할 상품 ID", example = "1")
    private Long productId;
}

