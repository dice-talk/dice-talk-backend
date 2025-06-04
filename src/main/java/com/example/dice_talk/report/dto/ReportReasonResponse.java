package com.example.dice_talk.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(name = "ReportReasonResponse", description = "신고 사유 응답 DTO")
@Getter
@AllArgsConstructor
public class ReportReasonResponse {
    @Schema(description = "신고 사유 코드", example = "SPAM")
    private String code;

    @Schema(description = "신고 사유 설명", example = "스팸성 메시지 (광고·홍보 등)")
    private String description;
}
