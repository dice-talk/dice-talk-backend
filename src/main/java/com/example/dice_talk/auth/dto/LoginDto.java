package com.example.dice_talk.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LoginDto {
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String username;

    @Schema(description = "사용자 비밀번호", example = "password1234!")
    private String password;
}
