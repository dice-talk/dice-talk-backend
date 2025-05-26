package com.example.dice_talk.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;

@Schema(name = "emailDto", description = "이메일 인증 Dto")
@Data
@Getter
@Setter
public class EmailRequest {
    @Schema(description = "인증받을 이메일 주소")
    @Email
    private String email;
}
