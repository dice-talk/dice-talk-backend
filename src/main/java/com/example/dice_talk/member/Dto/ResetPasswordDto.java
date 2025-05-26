package com.example.dice_talk.member.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(name = "ResetPasswordDto", description = "비밀번호 재설정 DTO")
@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordDto {
    @Schema(description = "회원 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "새 비밀번호", example = "NewPass123!@#")
    private String newPassword;
}
