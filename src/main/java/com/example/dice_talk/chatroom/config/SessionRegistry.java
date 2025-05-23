package com.example.dice_talk.chatroom.config;

import com.example.dice_talk.chat.dto.UserInfo;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//세션 정보 저장 및 관리
@Component
public class SessionRegistry {
    // 채팅방 ID → (멤버 ID → 세션 ID) 매핑
    private final ConcurrentHashMap<String, Map<Long, String>> roomMemberSessionMap = new ConcurrentHashMap<>();

    // 세션 ID → 사용자 정보 매핑
    public final ConcurrentHashMap<String, UserInfo> sessionMemberMap = new ConcurrentHashMap<>();



    //사용자가 채팅방에 입장할 때 memberId와 세션 ID 매핑 저장
    //사용자가 채팅방에 처음 입장할 때 호출
    public void registerUserInChatRoom(String chatRoomId, Long memberId, String sessionId) {
        // 채팅방 ID에 해당하는 맵이 없으면 새로 생성하고, 멤버 ID와 세션 ID를 추가
        roomMemberSessionMap.computeIfAbsent(chatRoomId, k -> new ConcurrentHashMap<>())
                .put(memberId, sessionId);

        // 세션 정보 맵에 추가
        sessionMemberMap.put(sessionId, new UserInfo(memberId, chatRoomId));
    }
    public void registerSession(String sessionId, UserInfo userInfo) {
        sessionMemberMap.put(sessionId, userInfo);
    }

    //세션ID로 사용자 정보를 조회
    public UserInfo getUserInfo(String sessionId) {
        return sessionMemberMap.get(sessionId);
    }

    // 채팅방의 모든 세션 ID 조회
    public Map<Long, String> getSessionsInChatRoom(String chatRoomId) {
        return roomMemberSessionMap.getOrDefault(chatRoomId, Collections.emptyMap());
    }

    // 세션 제거
    public void removeSession(String sessionId) {
        UserInfo userInfo = sessionMemberMap.remove(sessionId);
        if (userInfo != null) {
            String chatRoomId = userInfo.getChatRoomId();
            Long memberId = userInfo.getMemberId();

            Map<Long, String> memberSessionMap = roomMemberSessionMap.get(chatRoomId);
            if (memberSessionMap != null) {
                memberSessionMap.remove(memberId);

                // 채팅방에 사용자가 없으면 채팅방 맵에서 제거
                if (memberSessionMap.isEmpty()) {
                    roomMemberSessionMap.remove(chatRoomId);
                }
            }
        }
    }
}
