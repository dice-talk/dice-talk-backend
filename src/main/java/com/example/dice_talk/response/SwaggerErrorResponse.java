package com.example.dice_talk.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class SwaggerErrorResponse {
    @Schema(description = "에러 코드", example = "UNAUTHORIZED")
    private String error;

    @Schema(description = "에러 메시지", example = "로그인이 필요합니다.")
    private String message;
}
