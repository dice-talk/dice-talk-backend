package com.example.dice_talk.chat.service;

import com.example.dice_talk.chat.dto.UserInfo;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.repository.ChatRepository;
import com.example.dice_talk.chatroom.config.SessionRegistry;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.example.dice_talk.chatroom.config.StompHandler.saveSessionInfo;


@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;
    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRegistry sessionRegistry;

    //사용자가 채팅방에 입장할 때 호출되는 메서드
    public void enterChatRoom(long roomId, Long memberId, String sessionId) {

        // 사용자 등록 (세션 ID와 사용자 정보 매핑)
        sessionRegistry. registerUserInChatRoom(String.valueOf(roomId), memberId, sessionId);

        // StompHandler에 사용자 정보 저장 (전역 관리)
        saveSessionInfo(sessionId, memberId);

        // 해당 채팅방을 구독 중인 모든 클라이언트에게 입장 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/" + roomId, "Dice 분들이 모두 입장하였습니다.");
    }


    //메세지 생성 -> Repository 에 저장, DB에 저장된 메세지를 반환하여 클라이언트에 응답
    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }
    //메세지 찾기
    public Chat findChat(long chatId) {
        return chatRepository.findById(chatId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.CHAT_NOT_FOUND));
    }


    // memberId로 사용자 구독 취소
    // 특정 채팅방에서 memberId를 가진 사용자의 구독 취소
    public void unsubscribeUserByMemberId(Long memberId, String chatRoomId) {
        // 해당 채팅방의 채팅방Id 를 가져옴
        Map<Long, String> memberSessionMap = sessionRegistry.getSessionsInChatRoom(chatRoomId);

        // 채팅방이 존재하고 해당 member가 있는 경우에만 처리
        if (memberSessionMap != null && memberSessionMap.containsKey(memberId)) {
            // memberId에 해당하는 세션Id 가져옴
            String sessionId = memberSessionMap.get(memberId);
            // 클라이어트에게 구독 취소 메세지 전송
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/sub/chat/" + chatRoomId,
                    "채팅방이 종료되었습니다."
            );

            // SessionRegistry에서 세션 제거
            UserInfo userInfo = sessionRegistry.getUserInfo(sessionId);
            if (userInfo != null) {
                sessionRegistry.removeSession(sessionId);


            }
        }
    }


    //채팅방의 모든 사용자 구독 취소
    public void unsubscribeAllUsersFromChatRoom(Long chatRoomId) {
        //특정 채팅방의 모든 사용자 세션 정보를 가져옴
        Map<Long, String> memberSessionMap = sessionRegistry.getSessionsInChatRoom(chatRoomId.toString());
        if (memberSessionMap != null) {
            // 맵을 복사하여 ConcurrentModificationException 방지 (원본 맵 수정되는 것 방지)
                //컬렉션을 순회하면서 순회하는 대상 컬렉션이 수정되어 값이 서로 다를 경우 발생
            Map<Long, String> copyMap = new HashMap<>(memberSessionMap);

            //항목(entry)에 대해 반복
            for (Map.Entry<Long, String> entry : copyMap.entrySet()) {
                //Entry는 Map.Entry 객체로, 키-값 쌍을 나타냄
                //getKey() -> memberId를 getValue()-> sessionId(값)을 얻는다.
                String sessionId = entry.getValue();

                // 클라이언트에게 구독 취소 메시지 전송
                messagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/queue/unsubscribe" + chatRoomId,
                        "채팅방이 종료되었습니다."
                );

                //세션 제거
               sessionRegistry.removeSession(sessionId);
            }

            // 채팅방의 모든 구독자에게 채팅방 종료 메시지 전송
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + chatRoomId,
                    "채팅방이 종료되었습니다."
            );
        }
    }


}
