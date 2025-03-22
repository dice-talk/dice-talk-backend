package com.example.dice_talk.member.email;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    //이메일 인증 API
    private final EmailVerificationService verificationService;

    //이메일로 인증번호 전송
    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody VerificationRequest request) {
        verificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("인증번호가 이메일로 전송되었습니다.");
    }

    //입력한 인증번호 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody VerificationRequest request) {
        boolean isValid = verificationService.verifyCode(request.getEmail(), request.getCode());
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 올바르지 않거나 만료되었습니다.");
        }
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }
}
