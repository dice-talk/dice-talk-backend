package com.example.dice_talk.member.toss;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

//Toss인증 API와 통신해서 AccessToken 발급
@Service
public class TossAuthService {

    //Toss 인증 API에서 제공한 테스트용 클라이언트 ID, Secret
    private final String CLIENT_ID = "test_a8e23336d673ca70922b485fe806eb2d";
    private final String CLIENT_SECRET = "test_418087247d66da09fda1964dc4734e453c7cf66a7a9e3";

    //Access Token 발급받는 메서드
    public String getAccessToken() {
        //RestTemplate 객체 생성 -> HTTP 요청 보냄
        RestTemplate restTemplate = new RestTemplate();
        //Toss 인증 서버의 Access Token 발급하는 URI
        String url = "https://oauth2.cert.toss.im/token";

        //HTTP 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        //요청 본문 형식 -> "application/x-www-form-urlencoded"
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        //요청 본문에 포함될 데이터
        Map<String, String> body = new HashMap<>();
        //OAuth2 "Client credentials Grant" 방식 사용
        body.put("grant_type", "client_credentials");
        body.put("client_id", CLIENT_ID);
        body.put("client_secret", CLIENT_SECRET);
        //OAuth Scope : ca 인증기관
        body.put("scope", "ca");

        //HTTP 요청 Body +header 포한한 HttpEntity 객체 생성
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        //RestTemplate 사용으로 POST 요청 (요청보내는 Url, Http 메서드 Post 요청, 요청데이터(header+body), 응답데이터(Map)
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        //access Token 반환
        return (String) response.getBody().get("access_token");
    }

    //Toss 인증 서버에 간편인증 결과 조회 요청 -> Map으로 반환
    public Map<String, Object> getVerificationResult(String accessToken, String txId) {
        // RestTemplate 객체 생성: HTTP 클라이언트
        RestTemplate restTemplate = new RestTemplate();

        // Toss 인증 서버의 본인인증 결과를 조회하는 API URL
        String url = "https://cert.toss.im/api/v2/sign/user/auth/result";

        HttpHeaders headers = new HttpHeaders();
        // Authorization 헤더에 Bearer + 토큰(Access Token)을 설정 ->  인증
        headers.set("Authorization", "Bearer " + accessToken);

        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("txId", txId);

        // HTTP 요청 엔티티(Entity) 생성
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // RestTemplate으로 HTTP POST 요청을 보내고, 응답을 ResponseEntity로 받음
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        // 응답 본문 반환: 응답에서 본문(Body)을 Map 형태로 반환
        // 응답 본문에는 "이름, 생년월일, 성별, CI(Connecting Information)" 등 개인 정보가 포함됨
        return response.getBody();
    }

    //Toss 인증 API를 통해 본인 인증 요청을 생성, 인증에 필요한 정보 (인증URL, TXID) 반환하는 메서드
    public Map<String, String> createTossAuthRequest() {
        // Access Token 발급
        String accessToken = getAccessToken();

        // HTTP 메서드 가져올 RestTemplate 객체 생성
        RestTemplate restTemplate = new RestTemplate();

        // Toss 인증 서버의 본인인증 결과를 조회하는 API URL
        String url = "https://cert.toss.im/api/v2/sign/user/auth/request";

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);             // Bearer 인증 토큰 설정
        headers.setContentType(MediaType.APPLICATION_JSON); // Content-Type 설정

        // HTTP 요청 본문 설정
        Map<String, Object> body = new HashMap<>();
        body.put("requestType", "USER_NONE");          // 요청 타입 설정 (USER_NONE은 일반적인 사용자 인증 요청)

        // ✅ 앱에 맞는 딥링크로 수정
        body.put("successCallbackUrl", "dice-talk://toss-success"); // 인증 성공 시 콜백 URL
        body.put("failCallbackUrl", "dice-talk://toss-fail");    // 인증 실패 시 콜백 URL

        // HTTP 요청 엔티티 생성
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // RestTemplate로 POST 요청 보내고 응답 받기
        ResponseEntity<Map> response = restTemplate.exchange(
                url,                // 요청 URL
                HttpMethod.POST,    // HTTP 메서드
                request,            // 요청 엔티티
                Map.class           // 응답 타입
        );

        // 응답 본문 파싱
        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> success = (Map<String, Object>) responseBody.get("success");

        // 필요한 정보 추출
        Map<String, String> result = new HashMap<>();
        result.put("authUrl", (String) success.get("authUrl")); // 인증 URL 추출
        result.put("txId", (String) success.get("txId"));     // 트랜잭션 ID 추출

        //  결과 반환
        return result;
    }

}
