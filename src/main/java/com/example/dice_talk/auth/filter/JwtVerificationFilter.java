package com.example.dice_talk.auth.filter;


import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.auth.MemberDetailsService;
import com.example.dice_talk.auth.jwt.JwtTokenizer;
import com.example.dice_talk.auth.utils.AuthorityUtils;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final AuthorityUtils authorityUtils;
    private final MemberDetailsService memberDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer, AuthorityUtils authorityUtils, MemberDetailsService memberDetailsService, RedisTemplate<String, Object> redisTemplate) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
        this.memberDetailsService = memberDetailsService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            //  JWT를 검증하는 데 사용되는 private 메서드
            Map<String, Object> claims = verifyJws(request);
            // Redis에 저장된 토큰 있는지 확인하는 메서드
            isTokenValidInRedis(claims);
            //  Authentication 객체를 SecurityContext에 저장하기 위한 private 메서드
            setAuthenticationToContext(claims);
        } catch (SignatureException se) {
            request.setAttribute("exception", se);
        } catch (ExpiredJwtException ee) {
            request.setAttribute("exception", ee);
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }

        // JWT의 서명 검증에 성공하고, Security Context에 Authentication 저장한 뒤
        // 다음(Next) Security Filter 호출
        filterChain.doFilter(request, response);
    }

    // Filter 건너뛸지 말지
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        //  Authorization header의 값을 얻음.
        String authorization = request.getHeader("Authorization");
        // Authorization header의 값이 null이거나 Authorization header의 값이 “Bearer”로 시작하지 않는다면
        // 해당 Filter의 동작을 수행하지 않음
        return authorization == null || !authorization.startsWith("Bearer");
    }

    // JWT 검증
    private Map<String, Object> verifyJws(HttpServletRequest request) {
        //  request의 header에서 JWT를 얻음.
        // ->  클라이언트의 로그인 인증이 성공적으로 수행되면, 서버 측에서 Authorization header에 JWT를 추가하고
        // -->  여기는 클라이언트가 response header 로 전달받은 JWT를 request header에 추가해서 서버 측에 전송함.
        // 변수명 jws -> 서명된 JWT를 JWS(JSON Web Token Signed)라고 부르기 때문
        String jws = request.getHeader("Authorization").replace("Bearer ", "");
        //  JWT 서명(Signature)을 검증하기 위한 Secret Key
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        // JWT에서 Claims 파싱 -> 내부적으로 서명(Signature) 검증에 성공했다는 의미
        // Claims가 정상적으로 파싱이 되면 서명 검증 역시 자연스럽게 성공한거임.
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();

        return claims;
    }

    // Authentication 객체를 SecurityContext에 저장하기 위한 메서드
    private void setAuthenticationToContext(Map<String, Object> claims) {
        //  JWT에서 파싱 한 Claims에서 username 얻음.
        String username = (String) claims.get("username");
        // Claims에서 얻은 권한 정보를 기반으로 List<GrantedAuthority를 생성
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((List) claims.get("roles"));
        // UserDetailsService를 통해 MemberDetails 가져오기
        MemberDetailsService.MemberDetails memberDetails = (MemberDetailsService.MemberDetails) memberDetailsService.loadUserByUsername(username);
        //customPrincipal 객체 생성
        CustomPrincipal customPrincipal = new CustomPrincipal(username, memberDetails.getMemberId());
        // username과 List<GrantedAuthority를 포함한 Authentication 객체를 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(customPrincipal, null, authorities);
        // SecurityContext에 Authentication 객체를 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Redis에서 토큰 검증하는 기능 추가
    // void 로 안될 때 터뜨리기만 하면 됨.
    private void isTokenValidInRedis (Map<String, Object> claims){
        String username = Optional.ofNullable((String) claims.get("username"))
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USERNAME_NOT_FOUND));
        Boolean hasey = redisTemplate.hasKey(username);

        if(Boolean.FALSE.equals(hasey)){
            throw new IllegalStateException("Redis key Does Not Exist for username : " + username);
        }
    }
}
