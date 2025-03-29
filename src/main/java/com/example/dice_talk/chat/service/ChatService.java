package com.example.dice_talk.chat.service;

import com.example.dice_talk.chat.UserInfo;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.repository.ChatRepository;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;
    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    //닉네임과 세션 ID를 매핑하는 저장소
    //chatRoomId -> (nickname : sessionId) 구조로 저장
    private final ConcurrentHashMap<String, Map<Long, String >> roomMemberSessionMap = new ConcurrentHashMap<>();
    //세션ID -> 사용자 정보 맵
    private final ConcurrentHashMap<String, UserInfo> sessionUserMap = new ConcurrentHashMap<>();

    /*메세지 생성 -> Repository 에 저장,
    DB에 저장된 메세지를 반환하여 클라이언트에 응답
     */
    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }
    //메세지 찾기
    public Chat findChat(long chatId) {
        return chatRepository.findById(chatId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.CHAT_NOT_FOUND));
    }


    //사용자가 채팅방에 입장할 때 memberId와 세션 ID 매핑 저장
    //사용자가 채팅방에 처음 입장할 때 호출
    public void registerUserInChatRoom(String chatRoomId, Long memberId, String sessionId) {
       //채팅방 사용자 맵에 추가
        //computeIfAbsent() : key 존재 -> 기존 value 반환 / key 존재X -> 람다식으로 적용한 값을 해당 key에 저장, 새로운 value 반환
        roomMemberSessionMap.computeIfAbsent(chatRoomId, k -> new ConcurrentHashMap<>()).put(memberId, sessionId);

        //세션 정보 맵에 추가
        sessionUserMap.put(sessionId, new UserInfo(memberId, chatRoomId));
    }

    //memberId로 사용자 구독 취소
    //특정 채팅방에서 memberId를 가진 사용자의 구독 취소
    public void unsubscribeUserByMemberId(Long memberId, String chatRoomId) {
        //해당 채팅방의 채팅방Id 를 가져옴
        Map<Long, String> memberSessionMap = roomMemberSessionMap.get(chatRoomId);

        //채팅방이 존재하고 해당 member가 있는 경우에만 처리
        if (memberSessionMap != null && memberSessionMap.containsKey(memberId)) {
            //닉네임에 해당하는 세션Id 가져옴
            String sessionId = memberSessionMap.get(memberId);
            //클라이어트에게 구독 취소 메세지 전송
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/sub/chat/" + chatRoomId,
                    "채팅방이 종료되었습니다."
            );
            //서버 측 맵에서 사용자 정보 제거
            memberSessionMap.remove(sessionId);
        }
    }

    //채팅방의 모든 사용자 구독 취소
    public void unsubscribeAllUsersFromChatRoom(Long chatRoomId) {
        //특정 채팅방의 모든 사용자 세션 정보를 가져옴
        Map<Long, String> memberSessionMap = roomMemberSessionMap.get(chatRoomId.toString());
        if (memberSessionMap != null) {
            // 맵을 복사하여 ConcurrentModificationException 방지 (원본 맵 수정되는 것 방지)
                //컬렉션을 순회하면서 순회하는 대상 컬렉션이 수정되어 값이 서로 다를 경우 발생
            Map<Long, String> copyMap = new HashMap<>(memberSessionMap);

            //항목(entry)에 대해 반복
            for (Map.Entry<Long, String> entry : copyMap.entrySet()) {
                //Entry는 Map.Entry 객체로, 키-값 쌍을 나타냄
                //getKey() -> memberId를 getValue()-> sessionId(값)을 얻는다.
                Long memberId = entry.getKey();
                String sessionId = entry.getValue();

                // 클라이언트에게 구독 취소 메시지 전송
                messagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/queue/unsubscribe" + chatRoomId,
                        "채팅방이 종료되었습니다."
                );

                //전체 세션 정보 맵에서 제거
                sessionUserMap.remove(sessionId);
            }

            // 채팅방 사용자 맵에서 제거
            roomMemberSessionMap.remove(chatRoomId.toString());

            // 채팅방의 모든 구독자에게 채팅방 종료 메시지 전송
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + chatRoomId,
                    "채팅방이 종료되었습니다."
            );
        }
    }


}
