package com.example.dice_talk.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        RestTemplate restTemplate = builder.build();
        
        // 로깅 인터셉터 추가
        restTemplate.getInterceptors().add((request, body, execution) -> {
            System.out.println("Request URL: " + request.getURI());
            System.out.println("Request Method: " + request.getMethod());
            System.out.println("Request Headers: " + request.getHeaders());
            System.out.println("Request Body: " + new String(body, StandardCharsets.UTF_8));
            
            ClientHttpResponse response = execution.execute(request, body);
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Headers: " + response.getHeaders());
            
            return response;
        });
        
        return restTemplate;
    }
}
