package com.example.dice_talk.payment.controller;

import com.springboot.payment.entity.PaymentMaster;
import com.springboot.payment.service.PaymentMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "밥풀 상점", description = "밥풀 상점 상품 관련 API")
@RestController
@RequestMapping("/api/v1/payments/master")
@RequiredArgsConstructor
public class PaymentMasterController {

    private final PaymentMasterService paymentMasterService;

    @Operation(summary = "밥풀 상품 전체 조회", description = "밥풀 상점에 등록된 모든 상품을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "밥풀 상점 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
    })
    @GetMapping
    public ResponseEntity getAllMasters() {
        List<PaymentMaster> masters = paymentMasterService.findAllMasters();
        return ResponseEntity.ok(masters);
    }
}
