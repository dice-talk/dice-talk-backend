package com.example.dice_talk.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmResponseDto {
    // 성공 시 예시 필드
    private String paymentKey;
    private String status;     // READY, DONE, CANCELED 등
    private String orderId;
    private Long totalAmount;
    private String requestedAt;
    private String approvedAt;
    private String method;

    // 오류 시 예시 필드
    private String code;       // 예: "PAYMENTS_CONFIRM_FAILED"
    private String message;    // 예: "결제 금액이 올바르지 않습니다."
}
