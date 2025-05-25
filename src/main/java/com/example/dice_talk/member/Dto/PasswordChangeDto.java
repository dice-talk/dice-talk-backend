package com.example.dice_talk.member.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDto {
    @Schema(description = "기존 비밀번호", example = "useR234@")
    @NotBlank
    private String oldPassword;

    @Schema(description = "새 비밀번호", example = "NewPass123!@#")
    @NotBlank
    private String newPassword;
}
