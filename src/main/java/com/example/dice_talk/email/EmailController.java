package com.example.dice_talk.email;

import com.example.dice_talk.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
//@RequiredArgsConstructor
@RequestMapping("/auth")
public class EmailController {

    //이메일 인증 API
    private final EmailService emailService;
    private final MemberService memberService;

    public EmailController(EmailService emailService, MemberService memberService) {
        this.emailService = emailService;
        this.memberService = memberService;
    }

    //이메일로 인증번호 전송
    @PostMapping("/email")
    public ResponseEntity<String> sendVerificationCode(@RequestBody VerificationRequest request) {
        emailService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("인증번호가 이메일로 전송되었습니다.");
    }

    //입력한 인증번호 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody VerificationRequest request) {
        //이메일 중복 확인
        memberService.verifyExistsEmail(request.getEmail());
        boolean isValid = emailService.verifyCode(request.getEmail(), request.getCode());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 올바르지 않거나 만료되었습니다.");
        }

        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

}
