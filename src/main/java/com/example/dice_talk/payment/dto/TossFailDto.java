package com.example.dice_talk.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "토스 결제 실패 DTO")
@Getter
@Setter
public class TossFailDto {
    @Schema(description = "주문 ID", example = "order_1234567890")
    private String orderId;

    @Schema(description = "토스 결제 키", example = "test_payment_key_1234567890")
    private String paymentKey;

    @Schema(description = "실패 사유", example = "사용자가 결제를 취소했습니다.")
    private String message;  // 실패 사유
}