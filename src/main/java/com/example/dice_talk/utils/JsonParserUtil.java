package com.example.dice_talk.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonParserUtil {
    private final ObjectMapper objectMapper;

    public <T> T parse(String jsonString, Class<T> valueType){
        try {
            return objectMapper.readValue(jsonString, valueType);
        } catch (Exception e){
            throw new RuntimeException("JSON 파싱 실패 : " + e.getMessage(), e);
        }
    }
}
