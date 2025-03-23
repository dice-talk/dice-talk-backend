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
}
