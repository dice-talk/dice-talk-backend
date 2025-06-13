package com.example.dice_talk.payment.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.config.TossPaymentConfig;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.payment.dto.*;
import com.example.dice_talk.payment.entity.Payment;
import com.example.dice_talk.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "결제", description = "토스 결제 관련 API")
@RestController
@RequestMapping("/api/v1/payments/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final PaymentService paymentService;
    private final TossPaymentConfig tossPaymentConfig;

    @Operation(summary = "결제 요청", description = "결제 요청을 생성하고 결제창에 필요한 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/request")
    public ResponseEntity<PaymentResponseDto> requestTossPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal,
            @Parameter(description = "결제 요청 정보") @RequestBody PaymentRequestDto dto) {
        Payment payment = paymentService.requestTossPayment(dto, customPrincipal.getMemberId());

        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .clientKey(tossPaymentConfig.getTestClientApiKey())
                .orderName("다이스 " + payment.getDiceAmount() + "개 충전")
                .successUrl(tossPaymentConfig.getSuccessUrl())
                .failUrl(tossPaymentConfig.getFailUrl())
                .build();

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "토스 결제 승인", description = "결제 성공 시 토스 API로 결제를 승인하고, 결제 상태를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 승인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/confirm")
    public ResponseEntity<ConfirmPaymentResponseDto> confirmPayment(
            @Parameter(description = "결제 승인 정보") @RequestBody TossSuccessDto dto) {
        ConfirmPaymentResponseDto responseDto = paymentService.confirmPayment(dto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "결제 실패", description = "결제 실패 시 결제 상태를 실패로 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 실패 처리 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/fail")
    public ResponseEntity<Void> tossPaymentFail(
            @Parameter(description = "결제 실패 정보") @RequestBody TossFailDto failDto) {
        paymentService.failPayment(failDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "결제 취소", description = "결제 취소 시 결제 상태를 취소로 업데이트하고 포인트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 취소 처리 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/cancel")
    public ResponseEntity<Void> tossPaymentCancel(
            @Parameter(description = "결제 취소 정보") @RequestBody TossCancelDto cancelDto) {
        paymentService.cancelPayment(cancelDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "결제 내역 조회", description = "로그인한 회원의 모든 결제 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 내역 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/history")
    public ResponseEntity<List<PaymentHistoryDto>> getChargingHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        List<PaymentHistoryDto> historyDtos = paymentService.historyPayment(customPrincipal.getMemberId());
        return ResponseEntity.ok(historyDtos);
    }
}