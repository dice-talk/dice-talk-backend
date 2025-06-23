package com.example.dice_talk.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "결제 많이 한 사용자", example = "{\"memberId\": 1, \"email\": \"top1@example.com\", \"totalAmount\": 150000}" )
@Getter
@AllArgsConstructor
public class TopPayerDto {
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "회원 이메일", example = "top1@example.com")
    private String email;

    @Schema(description = "총 결제 금액", example = "150000")
    private int totalAmount;
}
