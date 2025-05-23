package com.example.dice_talk.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenizer {
    private final RedisTemplate<String, Object> redisTemplate;

    @Getter
    @Value("${jwt.key}")
    private String secretKey;

    @Getter
    @Value("${jwt.access-token-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    @Getter
    @Value("${jwt.refresh-token-expiration-minutes}")
    private int refreshTokenExpirationMinutes;

    public JwtTokenizer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Plain형태 Secret Key Base64형식으로 인코딩
    public String encodeBase64SecretKey(String secretKey){
        return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 인증 후 JWT 최초 발급용 생성 메서드
    public String generateAccessToken(Map<String, Object> claims,
                                      String subject,
                                      Date expiration,
                                      String base64EncodedSecretKey){
        // Base64 형식 Secret Key 문자열을 이용해 Key 객체 생성
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        String accessToken =  Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Calendar.getInstance().getTime())
                .setExpiration(expiration)
                .signWith(key)
                .compact();

        // 식별하기 위한 유저 정보 필요
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set((String) claims.get("username"), accessToken, accessTokenExpirationMinutes, TimeUnit.MINUTES);
        return accessToken;
    }

    // refresh Token 생성 메서드
    public String generateRefreshToken(String subject, Date expiration, String base64EncodedSecretKey, String accessToken){
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        String refreshToken =  Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Calendar.getInstance().getTime())
                .setExpiration(expiration)
                .signWith(key)
                .compact();

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(accessToken, refreshToken, refreshTokenExpirationMinutes, TimeUnit.MINUTES);
        return refreshToken;
    }

    // jws에서 claims 추출 메서드
    public Jws<Claims> getClaims(String jws, String base64EncodedSecretKey){
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        // JWS 파싱해서 Claims 추출
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jws);
    }

    // signature 위/변조 확인만 할때
    public void verifySignature(String jws, String base64EncodedSecretKey){
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jws);
    }

    // Token 만료일자 추출
    public Date getTokenExpiration(int expirationMinutes){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationMinutes);
        return calendar.getTime();
    }

    // 서명에 사용할 Secret Key 생성
    private Key getKeyFromBase64EncodedKey(String base64EncodedSecretKey){
        // Secret Key를 디코딩 한 후 byte array로 반환
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        // array 기반으로 HMAC 알고리즘 적용한 Key 객체 생성 후 반환
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 로그아웃 시 레디스에서 email을 기준으로 access, refresh 토큰 삭제 메서드
    public boolean deleteRegisterToken(String username){
        return Optional.ofNullable(redisTemplate.hasKey(username))
                .filter(Boolean::booleanValue)
                .map(hasKey -> {
                    String accessToken = (String) redisTemplate.opsForValue().get(username);
                    redisTemplate.delete(username);
                    redisTemplate.delete(accessToken);
                    return true;
                }).orElse(false);
    }
}
