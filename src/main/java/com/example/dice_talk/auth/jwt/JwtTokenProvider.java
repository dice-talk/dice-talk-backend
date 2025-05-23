package com.example.dice_talk.auth.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JwtTokenProvider
 * - 웹소켓 연결 시 JWT 토큰 검증 및 사용자 정보 추출 기능 제공
 * - 기존 JwtTokenizer를 활용하여 토큰 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtTokenizer jwtTokenizer;

    /**
     * JWT 토큰 검증
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            // 기존 JwtTokenizer의 verifySignature 메서드를 활용하여 토큰 검증
            String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
            jwtTokenizer.verifySignature(token, base64EncodedSecretKey);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
            Jws<Claims> claims = jwtTokenizer.getClaims(token, base64EncodedSecretKey);

            // 'memberId' 클레임으로 사용자 ID 추출
            Object memberId = claims.getBody().get("memberId");
            if (memberId != null) {
                // Integer, Long 등 다양한 형태로 저장될 수 있으므로 Long으로 변환
                if (memberId instanceof Integer) {
                    return ((Integer) memberId).longValue();
                } else if (memberId instanceof Long) {
                    return (Long) memberId;
                } else if (memberId instanceof String) {
                    return Long.parseLong((String) memberId);
                }
            }

            // memberId 클레임이 없는 경우, 대체 방법으로 subject 활용 가능
            String subject = claims.getBody().getSubject();
            if (subject != null && subject.matches("\\d+")) {
                return Long.parseLong(subject);
            }

            log.warn("Cannot extract member ID from token: {}", token);
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JWT 토큰에서 사용자 이름(username) 추출
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    public String getUsernameFromToken(String token) {
        try {
            String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
            Jws<Claims> claims = jwtTokenizer.getClaims(token, base64EncodedSecretKey);

            // 'username' 클레임에서 사용자 이름 추출
            return claims.getBody().get("username", String.class);
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JWT 토큰에서 클레임 값 조회
     * @param token JWT 토큰
     * @param claimName 클레임 이름
     * @return 클레임 값
     */
    public Object getClaimFromToken(String token, String claimName) {
        try {
            String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
            Jws<Claims> claims = jwtTokenizer.getClaims(token, base64EncodedSecretKey);

            return claims.getBody().get(claimName);
        } catch (Exception e) {
            log.error("Error extracting claim from token: {}", e.getMessage());
            return null;
        }
    }
}