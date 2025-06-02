package com.example.dice_talk.payment.service;

import com.example.dice_talk.payment.dto.PaymentRequest;
import com.example.dice_talk.payment.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Service
public class TossPaymentService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String secretKey;
    private final HttpHeaders headers;

    public TossPaymentService(RestTemplate restTemplate,
                              @Value("${toss.payments.base-url}") String baseUrl,
                              @Value("${toss.payments.secret-key}") String secretKey){
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.secretKey = secretKey;


        // -------------------------
        // 이 두 줄이 콘솔에 정확히 찍히는지 확인해 주세요.
        System.out.println(">>> [DEBUG] toss.baseUrl   = " + this.baseUrl);
        System.out.println(">>> [DEBUG] toss.secretKey = " + this.secretKey);
        // -------------------------
        // Basic Auth 헤더 준비
        String credential = secretKey + ":";
        String encoded = Base64Utils.encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encoded);
    }

    // 결제 생성 요청 : Toss Pay /v1/payments
    public PaymentResponse createPayment(PaymentRequest request){
        try {
            String url = baseUrl + "/v1/payments";
            System.out.println("Request URL: " + url);
            
            Map<String, Object> body = new HashMap<>();
            body.put("amount", request.getAmount());
            body.put("orderId", request.getOrderId());
            body.put("orderName", request.getOrderName());
            body.put("customerName", request.getCustomerName());
            body.put("successUrl", request.getSuccessUrl());
            body.put("failUrl", request.getFailUrl());
            
            System.out.println("Request Body: " + body);
            System.out.println("Headers: " + headers);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> respEntity = restTemplate.postForEntity(url, entity, Map.class);

            if (respEntity.getStatusCode().is2xxSuccessful() && respEntity.getBody() != null) {
                Map<String, Object> respBody = respEntity.getBody();
                PaymentResponse response = new PaymentResponse();
                response.setPaymentKey((String) respBody.get("paymentKey"));
                response.setNextRedirectUrl((String) respBody.get("nextRedirectUrl"));
                return response;
            } else {
                throw new RuntimeException("Toss 결제 준비 실패: HTTP " + respEntity.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Error Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Toss 결제 준비 실패: " + e.getMessage());
        }
    }
}
