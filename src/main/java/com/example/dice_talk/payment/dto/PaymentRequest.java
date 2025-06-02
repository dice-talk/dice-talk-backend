package com.example.dice_talk.payment.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PaymentRequest {
    @Min(value = 1, message = "amount는 1원 이상이어야 합니다.")
    private Long amount;       // 결제 금액(원 단위)

    @NotBlank(message = "orderId는 필수입니다.")
    private String orderId;    // 가맹점 주문 ID(앱 내에서 고유)

    @NotBlank(message = "orderName은 필수입니다.")
    private String orderName;  // 결제 화면에 노출될 주문명

    @NotBlank(message = "customerName은 필수입니다.")
    private String customerName; // 구매자 이름

    @NotBlank(message = "successUrl은 필수입니다.")
    private String successUrl;   // 결제 성공 후 리다이렉트할 URL

    @NotBlank(message = "failUrl은 필수입니다.")
    private String failUrl;      // 결제 실패 후 리다이렉트할 URL
}
