package com.example.dice_talk.payment.controller;

import com.example.dice_talk.payment.dto.ConfirmRequestDto;
import com.example.dice_talk.payment.dto.ConfirmResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class TossWidgetController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // Jackson ObjectMapper

    // application.yml 에 등록된 Toss 시크릿 키를 주입받습니다.
    @Value("${toss.payments.secret-key}")
    private String widgetSecretKey;

    @PostMapping("/confirm")
    public ResponseEntity<ConfirmResponseDto> confirmPayment(
            @RequestBody ConfirmRequestDto dto) {

        // 1) 요청 바디(맵) 생성
        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", dto.getPaymentKey());
        body.put("orderId",    dto.getOrderId());
        body.put("amount",     dto.getAmount());

        // 2) Basic Auth 헤더 준비 (시크릿 키 + ":" 을 Base64 인코딩)
        String credential = widgetSecretKey + ":";
        String encoded    = Base64Utils.encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encoded);

        // 3) HttpEntity 구성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 4) 리턴 받을 DTO 타입 정의
        //    ConfirmResponseDto 로 매핑하기 위해, respEntity.getBody()를 직접 매핑
        try {
            String url = "https://api.tosspayments.com/v1/payments/confirm";
            ResponseEntity<String> respEntity = restTemplate.postForEntity(url, entity, String.class);

            // 5) 응답 바디(JSON)를 ConfirmResponseDto로 변환
            String respBodyJson = respEntity.getBody();
            ConfirmResponseDto responseDto = objectMapper.readValue(respBodyJson, ConfirmResponseDto.class);

            return ResponseEntity
                    .status(respEntity.getStatusCode())
                    .body(responseDto);

        } catch (HttpClientErrorException ex) {
            // 4xx/5xx 오류 시, 에러 내용을 DTO에 담아 리턴하거나 예외 처리
            String errorJson = ex.getResponseBodyAsString();
            try {
                ConfirmResponseDto errorDto = objectMapper.readValue(errorJson, ConfirmResponseDto.class);
                return ResponseEntity
                        .status(ex.getStatusCode())
                        .body(errorDto);
            } catch (Exception e) {
                // JSON 파싱 실패 시 간단한 에러 DTO로 대체
                ConfirmResponseDto fallback = new ConfirmResponseDto();
                fallback.setStatus("FAIL");
                fallback.setMessage("API 호출 중 오류 발생: " + ex.getStatusCode());
                return ResponseEntity
                        .status(ex.getStatusCode())
                        .body(fallback);
            }
        } catch (Exception e) {
            // 그 외 예외
            ConfirmResponseDto fallback = new ConfirmResponseDto();
            fallback.setStatus("ERROR");
            fallback.setMessage("내부 서버 오류: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(fallback);
        }
    }
}

