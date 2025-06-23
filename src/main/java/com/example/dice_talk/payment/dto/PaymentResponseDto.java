package com.example.dice_talk.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "결제 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    @Schema(description = "주문 ID", example = "order_1234567890")
    private String orderId;     // 주문 ID (UUID)

    @Schema(description = "결제 금액", example = "10000")
    private int amount;         // 결제 금액

    @Schema(description = "토스 클라이언트 키", example = "test_ck_D4yKeq5bgrpKRd0J")
    private String clientKey;   // 토스 클라이언트 키

    @Schema(description = "주문명", example = "다이스 100개 충전")
    private String orderName;   // 예: "다이스 100개 충전"

    @Schema(description = "결제 성공 시 리다이렉트 URL", example = "dicetalkts://payment-success")
    private String successUrl;  // 성공 URL

    @Schema(description = "결제 실패 시 리다이렉트 URL", example = "dicetalkts://payment-fail")
    private String failUrl;     // 실패 URL
}

