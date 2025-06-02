package com.example.dice_talk.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmRequestDto {
    private String paymentKey;  // 토스페이먼츠 결제 키
    private String orderId;     // 가맹점 주문 ID
    private Long amount;        // 결제 승인 금액(원 단위)
}
