package com.example.dice_talk.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAdminResponseDto {
    private String orderId;
    private String email;
    private Long memberId;
    private String productName;
    private int amount;
    private int diceAmount;
    private String paymentStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
}