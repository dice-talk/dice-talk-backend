package com.example.dice_talk.member.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {
    //이메일 인증요청 DTO
    //클라이언트에서 이메일 & 인증번호 전송할때 사용
    private String email;
    private String code;
}
