package com.example.dice_talk.email;

import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
public class EmailController {

    //이메일 인증 API
    private final EmailService emailService;
    private final MemberService memberService;

    @Operation(summary = "이메일 인증번호 전송", description = "작성한 이메일로 인증번호를 전송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증번호 전송 성공",
                            content = @Content(schema = @Schema(implementation = SingleResponseDto.class),
                                    examples = @ExampleObject(value = "{\"data\": \"인증번호가 이메일로 전송되었습니다.\"}"))),
                    @ApiResponse(responseCode = "400", description = "잘못된 이메일 형태",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"BAD REQUEST\", \"message\": \"Invalid request format\"}")))}
    )
    //이메일로 인증번호 전송
    @PostMapping("/email")
    public ResponseEntity sendVerificationCode(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "인증번호를 받을 이메일 주소",
                                                  required = true, content = @Content(schema = @Schema(implementation = EmailRequest.class)))
                                                   @RequestBody EmailRequest request) {
        emailService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(new SingleResponseDto<>("인증번호가 이메일로 전송되었습니다."));
    }

    @Operation(summary = "이메일 검증(중복 검사)", description = "전송된 인증번호를 입력하여 인증 받습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이메일 인증 성공",
                            content = @Content(schema = @Schema(implementation = SingleResponseDto.class),
                                    examples = @ExampleObject(value = "{\"data\": \"이메일 인증이 완료되었습니다.\"}"))),
                    @ApiResponse(responseCode = "400", description = "잘못된 인증번호",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"BAD REQUEST\", \"message\": \"Invalid request format\"}")))}
    )
    //입력한 인증번호 검증
    @PostMapping("/verify-code")
    public ResponseEntity verifyCode(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "이메일과 인증번호",
                                    required = true, content = @Content(schema = @Schema(implementation = VerificationRequest.class)))
                                         @RequestBody VerificationRequest request) {

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

    @Operation(summary = "이메일 검증(중복 검사 X)", description = "전송된 인증번호를 입력하여 인증 받습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이메일 인증 성공",
                            content = @Content(schema = @Schema(implementation = SingleResponseDto.class),
                                    examples = @ExampleObject(value = "{\"data\": \"이메일 인증이 완료되었습니다.\"}"))),
                    @ApiResponse(responseCode = "400", description = "잘못된 인증번호",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"BAD REQUEST\", \"message\": \"Invalid request format\"}")))}
    )
    //입력한 인증번호 검증 + 존재하는 회원인지 검증(이메일)
    @PostMapping("/verify-code-email")
    public ResponseEntity verifyCodeAndEmail(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "이메일과 인증번호",
            required = true, content = @Content(schema = @Schema(implementation = VerificationRequest.class)))
                                     @RequestBody VerificationRequest request) {

        // 테스트용 인증번호 바이패스 코드(임시)
        if(request.getCode().equals("111111")) return ResponseEntity.ok(new SingleResponseDto<>("이메일 인증이 완료되었습니다."));
        //이메일 중복 확인
        String validEmail = memberService.findValidEmail(request.getEmail());
        boolean isValid = emailService.verifyCode(validEmail, request.getCode());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 올바르지 않거나 만료되었습니다.");
        }

        return ResponseEntity.ok(new SingleResponseDto<>(String.format("이메일 인증이 완료되었습니다. : %s", validEmail)));
    }

}
