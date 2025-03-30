package com.example.dice_talk.chatroom.config;

import com.example.dice_talk.auth.jwt.JwtTokenizer;
import com.example.dice_talk.chat.UserInfo;
import com.example.dice_talk.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/* WebSocket 연결 시 JWT 토큰을 검증하는 핸들러

 - 사용자가 WebSocket을 통해 서버에 연결할 때(CONNECT) JWT 토큰을 검사하고 해당 사용자의 정보를 WebSocket 세션에 저장
 - 이후 메세지를 주고받을 때, 해당 정보를 기반으로 사용자를 식별

 1. 클라이언트가 WebSocket에 연결 (`ws://localhost:8080/ws-stomp`)
 2. 헤더에 JWT 토큰을 포함
 3. 서버에서 토큰 검증하고 사용자의 `username`, `memberId` 저장
 4. 이후 채팅을 보낼 때, 저장된 `memberId`를 활용해 사용자 식별
* */

//인증 및 WebSocket 연결관리

@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenizer jwtTokenizer;
    private final MemberService memberService;
    private final SessionRegistry sessionRegistry;
    private static final Map<String, Long> sessionMemberMap = new ConcurrentHashMap<>();
    // WebSocket 메세지가 전송되기 전에 호출되는 메서드
    // 클라이언트가 처음 연결할 때(CONNECT) JWT 토큰을 검증하고 사용자 정보를 저장
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 메세지에서 STOMP 관련 정보 추출
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 클라이언트가 WebSocket에 처음 연결할 때(CONNECT) 실행
        if(StompCommand.CONNECT.equals(accessor.getCommand())){
            // JWT 토큰을 Authorization 헤더에서 가져오기 (Bearer {token} 형태이므로 앞의 "Bearer" 제거)
            String token = Objects.requireNonNull(accessor.getFirstNativeHeader("Authorization")).substring(7);
            if(token == null){
                throw new AccessDeniedException("Token is missing"); // 토큰 없으면 예외 발생
            }
            try{
                // JWT 토큰 검증
                String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
                Jws<Claims> claimsJws = jwtTokenizer.getClaims(token, base64EncodedSecretKey);
                Claims claims = claimsJws.getBody(); // 토큰에서 클레임 추출

                // 사용자 정보 추출(JwtAuthenticationFilter 에서 토큰 생성 시 "username", "memberId" 키로 저장함)
//                String username = claims.get("username", String.class);
                Long memberId = claims.get("memberId", Long.class);
//                String nickname = memberService.findNicknameByMemberId(memberId);
                // WebSocket 세션 ID 가져오기 (각 클라이언트는 고유한 세션 ID를 가짐)
                String sessionId = accessor.getSessionId();

                // SessionRegistry에 사용자 정보 저장
                sessionRegistry.registerSession(sessionId, new UserInfo(memberId, null));

                // 정적 메서드를 위한 정보 저장 (하위 호환성 유지)
                saveSessionInfo(sessionId, memberId);

                // WebSocket 세션에 사용자 정보 저장(메세지 핸들러에서 사용 가능)
                accessor.getSessionAttributes().put("memberId", memberId);
            } catch (Exception e){
                throw new AccessDeniedException("Invalid token"); // 토큰 검증 실패 시 예외 발생
            }
        //DISCONNECT 이벤트 발생 시 SessionRegistry 에서 세션 정보 제거
        } else if(StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            //연결 종료 시 세션 정보 제거
            String sessionId = accessor.getSessionId();
            if(sessionId != null) {
                sessionRegistry.removeSession(sessionId);
            }

        }
        return message;
    }

    // 세션 ID를 기반으로 memeberId를 가져오는 메서드
    // 메세지를 저장할 때 memberId를 사용하여 해당 사용자 식별 가능
    public static Long getMemberIdBySessionId(String sessionId){

        return sessionMemberMap.get(sessionId);
    }

    //새로운 WebSocket 연결이 수립될 때 호출되어, 해당 세션의 사용자 정보를 저장
    public static void saveSessionInfo(String sessionId, Long memberId) {
        sessionMemberMap.put(sessionId, memberId);
    }

}
