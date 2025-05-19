package com.example.dice_talk.email;

import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class EmailController {

    //이메일 인증 API
    private final EmailService emailService;
    private final MemberService memberService;

    //이메일로 인증번호 전송
    @PostMapping("/email")
    public ResponseEntity sendVerificationCode(@RequestBody VerificationRequest request) {
        emailService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(new SingleResponseDto<>("인증번호가 이메일로 전송되었습니다."));
    }

    //입력한 인증번호 검증
    @PostMapping("/verify-code")
    public ResponseEntity verifyCode(@RequestBody VerificationRequest request) {

        // 테스트용 인증번호 바이패스 코드(임시)
        if(request.getCode().equals("111111")) return ResponseEntity.ok(new SingleResponseDto<>("이메일 인증이 완료되었습니다."));
        //이메일 중복 확인
        memberService.verifyExistsEmail(request.getEmail());
        boolean isValid = emailService.verifyCode(request.getEmail(), request.getCode());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 올바르지 않거나 만료되었습니다.");
        }

        return ResponseEntity.ok(new SingleResponseDto<>("이메일 인증이 완료되었습니다."));
    }



}
