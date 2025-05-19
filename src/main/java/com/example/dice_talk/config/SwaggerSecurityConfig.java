package com.example.dice_talk.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
        name = "JWT",                       // SecurityRequirement 에서 참조할 이름
        type = SecuritySchemeType.HTTP,     // HTTP 방식 인증
        scheme = "bearer",                  // 헤더에 Authorization: Bearer (token)
        bearerFormat = "JWT"                // 형식 명시 (Swagger 에서 힌트처럼 사용)
)
@Configuration
public class SwaggerSecurityConfig {
}
