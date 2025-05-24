package com.example.dice_talk.member.toss;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.Dto.ResetPasswordDto;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Auth", description = "Toss 본인인증 및 계정 복구 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class TossAuthController {
    private final static String PASSWORD_DEFAULT_URI = "/auth/recover/password";
    private final MemberService memberService;
    private final TossAuthService tossAuthService;

    @Operation(summary = "본인인증 결과 조회", description = "Toss 인증 결과를 조회하여 사용자 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"name\":\"홍길동\",\"birth\":\"1990-01-01\",\"gender\":\"MALE\",\"ci\":\"CI_CODE\"}"))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))
            )
    })
    @PostMapping("/cert")
    public ResponseEntity<Map<String, Object>> getCertResult(
            @Parameter(description = "인증 거래 ID(txId)", example = "abc123") @RequestParam String txId) {
        // Toss Access Token 발급
        String accessToken = tossAuthService.getAccessToken();

        // Toss 서버에서 본인 인증 결과 조회
        Map<String, Object> result = tossAuthService.getVerificationResult(accessToken, txId);

        // 필요한 데이터 추출
        Map<String, Object> response = new HashMap<>();
        response.put("name", result.get("name"));
        response.put("birth", result.get("birth"));
        response.put("gender", result.get("gender"));
        response.put("ci", result.get("ci"));

        return new ResponseEntity<>(response, HttpStatus.OK); // 본인 인증 결과 반환
    }

    @Operation(summary = "인증 요청 URL 생성", description = "Toss 본인인증 요청을 위한 URL을 생성하여 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 URL 반환",
                    content = @Content(schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"url\":\"https://toss.im/auth?x=...\"}"))
            )
    })
    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestAuthUrl(){
        Map<String, String> response = tossAuthService.createTossAuthRequest();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //이메일 찾기 로직
    @Operation(summary = "이메일 찾기", description = "인증된 CI로 등록된 회원 이메일을 조회하여 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 반환",
                    content = @Content(schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"email\":\"user@example.com\"}"))
            ),
            @ApiResponse(responseCode = "404", description = "등록된 회원 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))
            )
    })
    @PostMapping("/recover/email")
    public ResponseEntity<Map<String, Object>> findEmail(
            @Parameter(description = "인증 거래 ID(txId)", example = "abc123") @RequestParam String txId) {
        // Toss Access Token 발급
        String accessToken = tossAuthService.getAccessToken();

        // Toss 서버에서 본인 인증 결과 조회
        Map<String, Object> result = tossAuthService.getVerificationResult(accessToken, txId);
        // Ci 통해서 등록 회원 찾기
        String ci = (String) result.get("ci");
        //등록된 회원인지 확인 (없다면 404)
        Member member = memberService.isCifindMember(ci);

        Map<String, Object> response = new HashMap<>();
        response.put("email", member.getEmail());

        //성공시 사용자의 이메일 반환
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //비밀번호 찾기 -> 성공시 비밀번호 재설정
    @Operation(summary = "비밀번호 찾기", description = "인증된 CI와 이메일 일치 시 비밀번호 재설정 URI를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "재설정 URI 반환",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "/auth/recover/password/1"))
            ),
            @ApiResponse(responseCode = "400", description = "이메일 불일치 또는 잘못된 파라미터",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))
            )
    })
    @PostMapping("/recover/password")
    public ResponseEntity<String> findPassword(
            @Parameter(description = "인증 거래 ID(txId)", example = "abc123") @RequestParam String txId,
            @Parameter(description = "회원 이메일", example = "user@example.com") @RequestParam String email
    ) {
        // Toss Access Token 발급
        String accessToken = tossAuthService.getAccessToken();

        // Toss 서버에서 본인 인증 결과 조회
        Map<String, Object> result = tossAuthService.getVerificationResult(accessToken, txId);

        //이메일 확인 및 반환 데이터
        String ci = (String) result.get("ci");
        //등록된 회원인지 확인 (없다면 404)
        Member member = memberService.isCifindMember(ci);

        //사용자가 입력한 이메일과 본인인증으로 찾은 이메일이 같지 않다면
        if(!member.getEmail().equals(email)) {
            throw new IllegalStateException("이메일이 잘못 입력되었습니다.");
        }

        URI location = UriCreator.createUri(PASSWORD_DEFAULT_URI, member.getMemberId());

        return ResponseEntity.created(location).body(member.getEmail());
    }

    @Operation(summary = "비밀번호 재설정", description = "URI로 전달 받은 회원 ID로 비밀번호를 재설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재설정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))
            )
    })
    @PostMapping("/resetting/password/{member-id}")
    public ResponseEntity<Void> resetPassword(@Parameter(description = "회원 ID", example = "1") @PathVariable("member-id") @Positive long memberId,
                                        @Parameter(description = "새 비밀번호 DTO", required = true)
                                        @RequestBody ResetPasswordDto resetDto){
        //비밀번호 재설정 로직
        memberService.resetPassword(memberId, resetDto);
        //비밀 번경 성공 응답
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

