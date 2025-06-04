package com.example.dice_talk.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;

@Configuration
//@OpenAPIDefinition(
//        servers = {
//                // HTTP로 접근할 수 있도록 서버 URL을 정의합니다.
//                @Server(url = "http://localhost:8080", description = "로컬 HTTP 서버"),
//                // 운영 환경이나 테스트 환경 URL도 추가로 적어 줄 수 있습니다.
//                @Server(url = "https://www.dicetalk.co.kr", description = "운영 서버")
//        }
//)
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(Arrays.asList(
                        new Server().url("https://www.dicetalk.co.kr").description("운영 서버"),
                        new Server().url("http://localhost:8080").description("로컬 개발 서버")
                ))
                .info(new Info()
                        .title("DiceTalk API")
                        .version("v1")
                        .description("DiceTalk API Documentation"))
                .components(new Components()
                        .addSchemas("SingleResponseDto", new Schema()
                                .type("object")
                                .addProperties("data", new Schema().type("object"))));
    }
}