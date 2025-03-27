package com.example.dice_talk.chatroom.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/* WebSocket 설정 파일
WebSocket 과 STOMP 프로토콜을 사용하여 실시간 채팅을 구현하기 위한 설정 클래스

역할 :
- 클라이언트(Web)와 서버간 WebSocket 통신을 설정
- STOMP 프로토콜을 기반으로 메세지를 주고받을 수 있도록 ㅂ로커 설정
- WebSocket 연결 시 JWT 토큰 검증(`StompHandler`)을 적용하여 인증 처리
* */
@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 WebSocket 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocket 연결 시 JWT 토큰을 검증하는 핸들러 (StompHandler에서 처리)
    private final StompHandler stompHandler;

    /* WebSocket STOMP 엔드포인트 설정

    역할 :
    - 클라이언트가 WebSocket을 연결할 때 사용할 엔드포인트(`/ws-stomp` 설정)
    - `SockJS` 사용으로 WebSocket을 지원하지 않는 환경에서도 폴백 가능 (ex. 오래된 브라우저)
    - CORS 정책을 적용하여 `http://localhost:3000`에서만 연결 가능
    * */

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp") // WebSocket 연결 엔드포인트 설정
                .setAllowedOrigins("http://localhost:3000") //특정 도메인에서만 WebSocket 허용 (보안 강화)  - CORS ??
                .withSockJS(); // WebSocket 을 지원하지 않는 환경에서도 사용 가능(폴백처리 - 폴링, 스트리밍 등)
    }

    /* 메세지 브로커 설정

    역할 :
    - 메세지를 주고받을 경로를 설정하여 클라이언트와 서버가 통신 가능하도록 구성

    메세지 흐름 :
    - 클라이언트가 메세지를 보낼 때 `/pub/{destination}` 경로를 사용
    - 서버가 메세지를 클라이언트에 전달할 때 `/sub/{destination}` 경로를 사용
    * */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub"); // 클라이언트가 구독할 경로(메세지 받을 때 사용)
        registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 메세지를 보낼 때 사용할 경로
    }

    /* 클라이언트에서 들어오는 메세지 처리 설정(JWT 인증 적용)

    역할 :
    - WebSocket 연결 시 클라이언트 요청을 가로채서 `StompHandler`를 통해 JWT 토큰을 검증
    - `CONNECT`요청을 가로채서 사용자 정보를 저장(인증된 사용자만 WebSocket 사용 가능)
    * */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler); // WebSocket 연결 시 JWT 인증 적용
    }
}
