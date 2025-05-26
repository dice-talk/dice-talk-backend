package com.example.dice_talk.auth.service;


import com.example.dice_talk.auth.jwt.JwtTokenizer;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AuthService {
    private final JwtTokenizer jwtTokenizer;
    private final RedisTemplate redisTemplate;

    public AuthService(JwtTokenizer jwtTokenizer, RedisTemplate redisTemplate) {
        this.jwtTokenizer = jwtTokenizer;
        this.redisTemplate = redisTemplate;
    }

    public boolean logout(String username){
        return jwtTokenizer.deleteRegisterToken(username);
    }

    // 토큰 재발급 로직
    public Map<String, String> reissueTokens(String accessToken, String refreshToken){
        // AccessToken 에서 사용자 정보 추출
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String username;
        try {
            username = (String) jwtTokenizer.getClaims(accessToken, base64EncodedSecretKey).getBody().get("username");
        } catch (Exception e){
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        // Redis 에서 저장된 Refresh Token 과 일치하는지 확인
        String storedRefreshToken = (String) redisTemplate.opsForValue().get(accessToken);
        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)){
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        // 새 Access/Refresh Token 발급
        Date newAccessTokenExp = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        Date newRefreshTokenExp = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());

        // 기존 claims 재사용
        Map<String, Object> claims = jwtTokenizer.getClaims(accessToken, base64EncodedSecretKey).getBody();

        String newAccessToken = jwtTokenizer.generateAccessToken(claims, username, newAccessTokenExp, base64EncodedSecretKey);
        String newRefreshToken = jwtTokenizer.generateRefreshToken(username, newRefreshTokenExp, base64EncodedSecretKey, newAccessToken);

        // Redis에 새 토큰 저장(기존 토큰 삭제)
        redisTemplate.delete(accessToken);
        redisTemplate.opsForValue().set(username, newAccessToken);
        redisTemplate.opsForValue().set(newAccessToken, newRefreshToken);

        // 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);
        return tokens;
    }
}
