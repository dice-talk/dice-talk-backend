package com.example.dice_talk.payment.controller;

import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.payment.dto.PaymentRequest;
import com.example.dice_talk.payment.dto.PaymentResponse;
import com.example.dice_talk.payment.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class TossPaymentController {

    private final TossPaymentService paymentService;

    @PostMapping("/ready")
    public ResponseEntity<SingleResponseDto<PaymentResponse>> readyPayment(
            @Validated @RequestBody PaymentRequest request
            ) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(new SingleResponseDto<>(response));
    }
}
