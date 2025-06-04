package com.example.dice_talk.payment.dto;

import com.example.dice_talk.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "결제 내역 DTO")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentHistoryDto {
    @Schema(description = "주문 ID", example = "order_1234567890")
    private String orderId;

    @Schema(description = "결제 금액", example = "10000")
    private int amount;

    @Schema(description = "충전된 다이스 수량", example = "100")
    private int diceAmount;

    @Schema(description = "결제 상태", example = "COMPLETED")
    private Payment.PaymentStatus paymentStatus;

    @Schema(description = "상품 이미지 URL", example = "https://example.com/image.jpg")
    private String productImage;

    @Schema(description = "상품명", example = "다이스 100개")
    private String productName;

    @Schema(description = "결제 요청 시간", example = "2024-03-20T14:30:00")
    private LocalDateTime requestedAt;

    @Schema(description = "결제 완료 시간", example = "2024-03-20T14:31:00")
    private LocalDateTime completedAt;
}