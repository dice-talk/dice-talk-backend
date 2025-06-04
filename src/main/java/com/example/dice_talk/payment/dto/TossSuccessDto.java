package com.example.dice_talk.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "토스 결제 성공 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TossSuccessDto {
    @Schema(description = "토스 결제 키", example = "test_payment_key_1234567890")
    private String paymentKey;

    @Schema(description = "주문 ID", example = "order_1234567890")
    private String orderId;

    @Schema(description = "결제 금액", example = "10000")
    private Long amount;
}
