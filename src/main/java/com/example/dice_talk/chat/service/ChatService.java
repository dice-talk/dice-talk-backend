package com.example.dice_talk.chat.service;

import com.example.dice_talk.chat.dto.ChatDto;
import com.example.dice_talk.chat.dto.UserInfo;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.mapper.ChatMapper;
import com.example.dice_talk.chat.repository.ChatRepository;
import com.example.dice_talk.chatroom.config.SessionRegistry;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.repository.ChatPartRepository;
import com.example.dice_talk.chatroom.repository.ChatRoomRepository;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.MemberRepository;
import com.example.dice_talk.pushNotification.entity.QPushNotificationToken;
import com.example.dice_talk.pushNotification.service.PushNotificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.dice_talk.chatroom.config.StompHandler.saveUserInfo;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;
    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRegistry sessionRegistry;
    private final PushNotificationTokenService pushNotificationTokenService;
    private final ChatPartRepository chatPartRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    //사용자가 채팅방에 입장할 때 호출되는 메서드
    public void enterChatRoom(long roomId, Long memberId, String sessionId) {

        // 사용자 등록 (세션 ID와 사용자 정보 매핑)
        sessionRegistry. registerUserInChatRoom(String.valueOf(roomId), memberId, sessionId);

        // StompHandler에 사용자 정보 저장 (전역 관리)
        saveUserInfo(sessionId, memberId);

        // 해당 채팅방을 구독 중인 모든 클라이언트에게 입장 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/" + roomId, "Dice 분들이 모두 입장하였습니다.");
    }

    // 시스템 사용자 조회 또는 생성 메서드
    private Member getOrCreateSystemMember(){
        return memberRepository.findByEmail("system@dice-talk.com").orElseGet(() -> {
            // 시스템 사용자가 없으면 생성
            Member systemMember = new Member();
            systemMember.setEmail("system@dice-talk.com");
            systemMember.setName("시스템");
            systemMember.setPassword("System1!");
            systemMember.setGender(Member.Gender.MALE);
            systemMember.setRegion("다이스톡 본사");
            systemMember.setBirth("2000-01-01");
            systemMember.setNotification(false);
            systemMember.setCi("000000-000000");
            systemMember.setAgeGroup("0");
            systemMember.setTotalDice(0);
            return memberRepository.save(systemMember);
        });
    }


    //메세지 생성 -> Repository 에 저장, DB에 저장된 메세지를 반환하여 클라이언트에 응답
    public Chat createChat(Chat chat) {
        Chat savedChat = chatRepository.save(chat);
        // --- 푸시 알림 발송 로직 추가 ---
        if (savedChat != null && savedChat.getMember() != null && savedChat.getChatRoom() != null) {
            Long chatRoomId = savedChat.getChatRoom().getChatRoomId(); // Chat 엔티티에 ChatRoom 객체가 있고, 그 안에 ID가 있다고 가정
            Long senderMemberId = savedChat.getMember().getMemberId();   // Chat 엔티티에 Member 객체가 있고, 그 안에 ID가 있다고 가정
            String senderNickname = savedChat.getNickname(); // 발신자 닉네임
            String messageContent = savedChat.getMessage();              // 메시지 내용

            // 1. 해당 채팅방에 참여하고 있고, 메시지 발신자가 아닌 다른 사용자들의 memberId 목록을 가져온다.
            List<Long> recipientMemberIds = getRecipientMemberIdsInChatRoom(chatRoomId, senderMemberId);

            // 2. 각 수신자에게 푸시 알림 발송
            if (!recipientMemberIds.isEmpty()) {
                String notificationTitle = "새 메시지: " + senderNickname;
                for (Long recipientId : recipientMemberIds) {
                    pushNotificationTokenService.sendNotificationToUser(
                            recipientId,
                            notificationTitle,
                            messageContent,
                            String.valueOf(chatRoomId) // 알림 데이터로 채팅방 ID 전달
                    );
                }
            }
        }
        // --- 푸시 알림 발송 로직 끝 ---
        return savedChat;
    }

    // 본인 제외한 채팅방 참여자 아이디 목록 반환
    private List<Long> getRecipientMemberIdsInChatRoom(Long chatRoomId, Long senderMemberId) {
        List<ChatPart> participantsInRoom = chatPartRepository.findByChatRoom_ChatRoomIdAndExitStatus(
                chatRoomId,
                ChatPart.ExitStatus.MEMBER_ENTER
        );

        if (participantsInRoom == null || participantsInRoom.isEmpty()) {
            System.out.println("[Push 알림] " + chatRoomId + "번 채팅방에 현재 참여 중인 사용자가 없습니다.");
            return Collections.emptyList();
        }

        // 참여자 목록에서 발신자를 제외하고 memberId만 추출하여 리스트로 만듭니다.
        return participantsInRoom.stream()
                .map(ChatPart::getMember) // ChatPart에서 Member 객체를 가져옴
                .filter(member -> member != null) // 혹시 Member 객체가 null인 경우 방지
                .map(Member::getMemberId)   // Member 객체에서 memberId를 가져옴
                .filter(memberId -> !memberId.equals(senderMemberId)) // 발신자 ID는 제외
                .distinct() // 중복 제거
                .collect(Collectors.toList());
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

    // 퇴장 메세지 전송 메서드
    public void sendExitMessage(Long chatRoomId, String nickname){
        try{
            Member system = getOrCreateSystemMember();

            // 퇴장 메세지 생성
            Chat exitChat = new Chat();
            exitChat.setMessage(nickname + " 님이 퇴장하셨습니다.");
            exitChat.setNickname("시스템");
            exitChat.setMember(system);

            // ChatRoom 설정
            exitChat.setChatRoom(chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHATROOM_NOT_FOUND)));

            // DB에 저장
            Chat savedExitChat = chatRepository.save(exitChat);

            // 접속자에게 전송
            ChatDto.Response exitMessage = new ChatDto.Response(
                    savedExitChat.getChatId(), savedExitChat.getMessage(), savedExitChat.getMember().getMemberId(), savedExitChat.getNickname(), savedExitChat.getChatRoom().getChatRoomId(), savedExitChat.getCreatedAt());
            messagingTemplate.convertAndSend("/sub/chat/" + chatRoomId, exitMessage);
            log.info("퇴장 메세지 전송 : {}님이 {}번 채팅방에서 퇴장", nickname, chatRoomId);
        } catch (Exception e){
            log.error("퇴장 메세지 전송 실패 : {}", e.getMessage());
        }
    }


}
