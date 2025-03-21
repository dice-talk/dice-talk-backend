package com.example.dice_talk.chatroom.principal;


import java.security.Principal;

/* StompPrincipal (STOMP 프로토콜에서 사용할 사용자 인증 객체
*
* 역할 :
* - WebSocket을 사용할 때 특정 상ㅇ자의 고유 ID (이름)를 저장하는 커스텀 Principal 클래스)
* - `java.security.Principal` 인터페이스를 구현하여 사용자의 식별정보를 제공
*
* 사용 목적 :
* - STOMP(WebSocket) 연결에서 사용자를 식별하기 위해 사용됨
* - JWT 기반 인증을 사용할 경우, 사용자의 ID(username)을 저장
* - `StompHandler`에서 사용자의 세션 정보와 연결*/
public class StompPrincipal implements Principal {

    // 사용자의 고유 ID 또는 이름 (username : email)
    private final String name;

    // 생성자, @param name 사용자의 고유 식별자(email)
    public StompPrincipal(String name) {
        this.name = name;
    }

    // 사용자의 이름(ID) 반환
    // - WebSocket 연결에서 사용자를 식별할 때 호출됨
    // - `Principal` 인터페이스의 `getName()`을 구현
    @Override
    public String getName() {
        return name;
    }
}
