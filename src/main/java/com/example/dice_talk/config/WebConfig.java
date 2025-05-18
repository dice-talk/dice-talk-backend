package com.example.dice_talk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  //모든 경로에 대해
                        .allowedOrigins("https://dicetalk.co.kr") //하용할 출처
                        .allowedMethods("GET", "POST", "PATCH", "DELETE")  //허용 HTTP 메서드
                        .allowedHeaders("*")  //모든 헤더 허용
                        .allowCredentials(true);   //쿠키 포함 허용 (JWT 등 인증 시 필수)
            }
        };
    }
}
