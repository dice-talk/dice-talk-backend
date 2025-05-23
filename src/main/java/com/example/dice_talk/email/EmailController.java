package com.example.dice_talk.email;

import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Email API", description = "이메일 인증 API 문서입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
public class EmailController {

    //이메일 인증 API
    private final EmailService emailService;
    private final MemberService memberService;

    @Operation(
            summary = "Email Post API",
            description = "답변을 등록합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "답변 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
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
