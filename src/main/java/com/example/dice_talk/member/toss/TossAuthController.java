package com.example.dice_talk.member.toss;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.Dto.ResetPasswordDto;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class TossAuthController {

    private final MemberService memberService;
   private final TossAuthService tossAuthService;

    @PostMapping("/cert")
    public ResponseEntity getCertResult(@RequestParam String txId) {
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

        return ResponseEntity.ok(response); // 본인 인증 결과 반환
    }

    //이메일 찾기 로직
    @PostMapping("/recover/email")
    public ResponseEntity findEmail(@RequestParam String txId) {
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
    @PostMapping("/recorver/password")
    public ResponseEntity findPassword(@RequestParam String txId,
                                       @RequestParam String email) {
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

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/resetting/password")
    public ResponseEntity resetPassword(@RequestBody ResetPasswordDto resetDto){
        //비밀번호 재설정 로직
        memberService.resetPassword(resetDto.getEmail(), resetDto.getNewPassword());
        //비밀 번경 성공 응답
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

