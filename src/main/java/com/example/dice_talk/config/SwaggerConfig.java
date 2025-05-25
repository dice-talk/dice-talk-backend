package com.example.dice_talk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
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