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

    @PostMapping("/cert")
    public ResponseEntity<Map<String, Object>> getCertResult(@RequestParam String txId) {
        try { // 👈 try-catch 시작
            String accessToken = tossAuthService.getAccessToken();
            Map<String, Object> tossResponse = tossAuthService.getVerificationResult(accessToken, txId); // 여기서 Exception 발생 가능

            // ... (이전 답변과 동일한 성공 및 오류 처리 로직) ...
            if (!"SUCCESS".equals(tossResponse.get("resultType"))) {
                System.err.println("Toss API Error in Controller (/cert): " + tossResponse);
                Map<String, Object> errorData = (Map<String, Object>) tossResponse.get("error");
                String reason = "Toss API returned an error.";
                if (errorData != null && errorData.get("reason") != null) {
                    reason = (String) errorData.get("reason");
                }
                Map<String, Object> clientErrorResponse = new HashMap<>();
                clientErrorResponse.put("error", reason);
                return new ResponseEntity<>(clientErrorResponse, HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> successData = (Map<String, Object>) tossResponse.get("success");
            if (successData == null) {
                System.err.println("Toss 응답에 success 객체가 없습니다 (/cert): " + tossResponse);
                throw new BusinessLogicException(ExceptionCode.TOSS_RESPONSE_ERROR);
            }

            Map<String, Object> personalData = (Map<String, Object>) successData.get("personalData");
            if (personalData == null) {
                System.err.println("Toss 응답의 success 객체 내에 personalData가 없습니다 (/cert): " + successData);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "개인 정보(personalData)를 찾을 수 없습니다.");
                errorResponse.put("userCiToken_if_available", successData.get("userCiToken"));
                return new ResponseEntity<>(errorResponse, HttpStatus.OK);
            }

            Map<String, Object> clientResponse = new HashMap<>();
            clientResponse.put("name", personalData.get("name"));
            clientResponse.put("birth", personalData.get("birthday"));
            clientResponse.put("gender", personalData.get("gender"));
            clientResponse.put("ci", personalData.get("ci"));
            clientResponse.put("ageGroup", personalData.get("ageGroup"));
            // clientResponse.put("phone", personalData.get("MOBILE_PHONE"));

            return new ResponseEntity<>(clientResponse, HttpStatus.OK);

        } catch (BusinessLogicException e) {
            System.err.println("BusinessLogicException in getCertResult (/cert): " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getExceptionCode().getStatus());
            errorResponse.put("errorMessage", e.getExceptionCode().getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(e.getExceptionCode().getStatus()));
        } catch (Exception e) { // 👈 TossAuthService.getVerificationResult()에서 던진 Exception 처리
            System.err.println("Unexpected error in getCertResult (/cert): " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Toss 인증 결과 처리 중 서버 내부 오류가 발생했습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } // 👈 try-catch 끝
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestAuthUrl(){
        // createTossAuthRequest는 Exception을 던지지 않으므로 try-catch 불필요 (내부에서 처리하거나 RuntimeException 발생 시)
        Map<String, String> response = tossAuthService.createTossAuthRequest();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/recover/email")
    public ResponseEntity<Map<String, Object>> findEmail(@RequestParam String txId) {
        try { // 👈 try-catch 시작
            String accessToken = tossAuthService.getAccessToken();
            Map<String, Object> tossResult = tossAuthService.getVerificationResult(accessToken, txId); // 여기서 Exception 발생 가능

            // CI 추출 전 tossResult의 유효성 검사 (resultType, success, personalData 등) 필요
            if (!"SUCCESS".equals(tossResult.get("resultType")) || tossResult.get("success") == null) {
                System.err.println("Toss API Error in findEmail: " + tossResult);
                throw new BusinessLogicException(ExceptionCode.TOSS_VERIFICATION_FAILED);
            }
            Map<String, Object> successData = (Map<String, Object>) tossResult.get("success");
            Map<String, Object> personalData = (Map<String, Object>) successData.get("personalData");

            if (personalData == null || personalData.get("ci") == null) {
                System.err.println("CI not found in Toss response for findEmail: " + tossResult);
                throw new BusinessLogicException(ExceptionCode.TOSS_CI_NOT_FOUND);
            }

            String ci = (String) personalData.get("ci"); // 복호화된 CI가 여기에 있어야 함
            Member member = memberService.isCifindMember(ci); // isCifindMember는 CI로 회원을 찾고 없으면 예외 발생 가정

            Map<String, Object> response = new HashMap<>();
            response.put("email", member.getEmail());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (BusinessLogicException e) {
            System.err.println("BusinessLogicException in findEmail: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getExceptionCode().getStatus());
            errorResponse.put("errorMessage", e.getExceptionCode().getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(e.getExceptionCode().getStatus()));
        } catch (Exception e) { // 👈 TossAuthService.getVerificationResult()에서 던진 Exception 처리
            System.err.println("Unexpected error in findEmail: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "이메일 찾기 처리 중 서버 내부 오류가 발생했습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } // 👈 try-catch 끝
    }

    @PostMapping("/recover/password")
    public ResponseEntity<String> findPassword(@RequestParam String txId, @RequestParam String email) {
        try { // 👈 try-catch 시작
            String accessToken = tossAuthService.getAccessToken();
            Map<String, Object> tossResult = tossAuthService.getVerificationResult(accessToken, txId); // 여기서 Exception 발생 가능

            // CI 추출 전 tossResult의 유효성 검사
            if (!"SUCCESS".equals(tossResult.get("resultType")) || tossResult.get("success") == null) {
                System.err.println("Toss API Error in findPassword: " + tossResult);
                throw new BusinessLogicException(ExceptionCode.TOSS_VERIFICATION_FAILED);
            }
            Map<String, Object> successData = (Map<String, Object>) tossResult.get("success");
            Map<String, Object> personalData = (Map<String, Object>) successData.get("personalData");

            if (personalData == null || personalData.get("ci") == null) {
                System.err.println("CI not found in Toss response for findPassword: " + tossResult);
                throw new BusinessLogicException(ExceptionCode.TOSS_CI_NOT_FOUND);
            }

            String ci = (String) personalData.get("ci"); // 복호화된 CI
            Member member = memberService.isCifindMember(ci);

            if (!member.getEmail().equals(email)) {
                // BusinessLogicException 사용 권장
                throw new BusinessLogicException(ExceptionCode.EMAIL_MISMATCH);
                // throw new IllegalStateException("이메일이 잘못 입력되었습니다."); // 기존 코드
            }

            URI location = UriCreator.createUri(PASSWORD_DEFAULT_URI, member.getMemberId());
            return ResponseEntity.created(location).body(member.getEmail()); // 또는 성공 메시지

        } catch (BusinessLogicException e) {
            System.err.println("BusinessLogicException in findPassword: " + e.getMessage());
            // 클라이언트에 전달할 오류 메시지 포맷팅
            // return new ResponseEntity<>(e.getExceptionCode().getMessage(), HttpStatus.valueOf(e.getExceptionCode().getStatus()));
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getExceptionCode().getStatus());
            errorResponse.put("errorMessage", e.getExceptionCode().getMessage());
            return new ResponseEntity<>(errorResponse.toString(), HttpStatus.valueOf(e.getExceptionCode().getStatus())); // String 반환 타입에 맞게 수정 필요
        } catch (Exception e) { // 👈 TossAuthService.getVerificationResult()에서 던진 Exception 처리
            System.err.println("Unexpected error in findPassword: " + e.getMessage());
            e.printStackTrace();
            // return new ResponseEntity<>("비밀번호 찾기 처리 중 서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "비밀번호 찾기 처리 중 서버 내부 오류가 발생했습니다.");
            return new ResponseEntity<>(errorResponse.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // String 반환 타입에 맞게 수정 필요
        } // 👈 try-catch 끝
    }

    @PostMapping("/resetting/password/{member-id}")
    public ResponseEntity<Void> resetPassword(@PathVariable("member-id") @Positive long memberId,
                                              @RequestBody ResetPasswordDto resetDto){
        // 이 메소드는 getVerificationResult를 직접 호출하지 않으므로,
        // memberService.resetPassword 내에서 발생하는 예외만 고려하면 됩니다.
        // (일반적으로 Service 계층에서 예외를 처리하거나 ControllerAdvise 등으로 전역 처리)
        memberService.resetPassword(memberId, resetDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
