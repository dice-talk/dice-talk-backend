package com.example.dice_talk.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponse {
    private String paymentKey; // Toss payments에서 발급해 주는 결제 키
    private String nextRedirectUrl; // 사용자를 리다이렉트시킬 결제 페이지 URL
}
