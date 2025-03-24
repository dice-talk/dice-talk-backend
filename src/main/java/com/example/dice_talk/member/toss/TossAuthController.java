package com.example.dice_talk.member.toss;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class TossAuthController {

    private TossAuthService tossAuthService;

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
}

