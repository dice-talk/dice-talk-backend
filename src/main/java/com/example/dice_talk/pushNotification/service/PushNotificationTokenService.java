package com.example.dice_talk.pushNotification.service;

import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.MemberRepository;
import com.example.dice_talk.pushNotification.entity.PushNotificationToken;
import com.example.dice_talk.pushNotification.repository.PushNotificationTokenRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PushNotificationTokenService {
    private final MemberRepository memberRepository;
    private final PushNotificationTokenRepository tokenRepository;
    private final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send"; // Expo 푸시 서버 URL

    public PushNotificationTokenService(MemberRepository memberRepository, PushNotificationTokenRepository tokenRepository) {
        this.memberRepository = memberRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void saveOrUpdateUserPushToken(Long memberId, String expoPushToken, String deviceType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId)); // 예외 처리 필요

        // 동일한 expoPushToken이 이미 다른 사용자에 의해 등록되었는지 먼저 확인 (정책에 따라 다름)
        // 만약 한 토큰은 무조건 한 사용자에게만 할당되어야 한다면,
        // 기존에 이 토큰을 사용하던 다른 사용자의 연결을 끊는 로직이 필요할 수 있습니다.
        // 여기서는 간단히, 해당 토큰이 존재하면 현재 사용자와 연결하거나, 없으면 새로 만듭니다.

        Optional<PushNotificationToken> existingTokenOpt = tokenRepository.findByExpoPushToken(expoPushToken);

        if (existingTokenOpt.isPresent()) {
            // 토큰이 이미 존재하면, 해당 토큰의 소유자를 현재 사용자로 업데이트 (또는 updatedAt만 갱신)
            PushNotificationToken tokenToUpdate = existingTokenOpt.get();
            tokenToUpdate.setMember(member); // 소유자 변경
            if (deviceType != null && !deviceType.isEmpty()) {
                tokenToUpdate.setDeviceType(deviceType);
            }
            // onUpdate()에 의해 updatedAt은 자동 갱신됨
            tokenRepository.save(tokenToUpdate);
            System.out.println("Push token updated for member: " + memberId);
        } else {
            // 새 토큰이면 새로 생성
            PushNotificationToken newToken = new PushNotificationToken();
            newToken.setMember(member);
            newToken.setExpoPushToken(expoPushToken);
            if (deviceType != null && !deviceType.isEmpty()) {
                newToken.setDeviceType(deviceType);
            }
            // onCreate()에 의해 createdAt, updatedAt 자동 설정됨
            tokenRepository.save(newToken);
            System.out.println("New push token saved for member: " + memberId);
        }
    }

    /**
     * 특정 사용자에게 푸시 알림을 발송합니다.
     * @param recipientMemberId 알림을 받을 사용자의 ID
     * @param title 알림 제목
     * @param messageBody 알림 본문
     * @param chatRoomId 알림 데이터 (예: 채팅방 ID)
     */
    public void sendNotificationToUser(Long recipientMemberId, String title, String messageBody, String chatRoomId) {
        List<PushNotificationToken> tokens = tokenRepository.findByMember_MemberId(recipientMemberId);
        if (tokens.isEmpty()) {
            System.out.println("No push tokens found for memberId: " + recipientMemberId);
            return;
        }

        for (PushNotificationToken token : tokens) {
            sendExpoPushNotification(token.getExpoPushToken(), title, messageBody, chatRoomId);
        }
    }

    /**
     * 단일 Expo 푸시 토큰으로 알림을 발송합니다.
     * @param expoPushToken 수신자 기기의 Expo 푸시 토큰
     * @param title 알림 제목
     * @param messageBody 알림 본문
     * @param chatRoomId 알림 데이터 (예: 채팅방 ID)
     */
    private void sendExpoPushNotification(String expoPushToken, String title, String messageBody, String chatRoomId) {
        if (expoPushToken == null || expoPushToken.isEmpty()) {
            System.out.println("Expo push token is missing.");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Accept-Encoding", "gzip, deflate");
        headers.set("Content-Type", "application/json");

        ExpoPushMessage pushMessage = new ExpoPushMessage(
                expoPushToken,
                title,
                messageBody,
                "default", // sound
                new ExpoPushMessage.Data(chatRoomId) // data
        );

        HttpEntity<ExpoPushMessage> requestEntity = new HttpEntity<>(pushMessage, headers);
        RestTemplate restTemplate = new RestTemplate(); // 실제 사용 시에는 Bean으로 등록해서 주입받는 것이 좋음

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(EXPO_PUSH_URL, requestEntity, String.class);
            System.out.println("Expo Push Notification sent to token " + expoPushToken.substring(0,15) + "...: " + response.getStatusCode());
            // TODO: 응답 본문에서 성공/실패 여부 (티켓 ID 등) 확인 및 로깅, 티켓 상태 추적 등
            // 예: {"data": [{"status": "ok", "id": "XXXX-XXXX-XXXX-XXXX"}]}
            // 예: {"data": [{"status": "error", "message": "...", "details": {...}}]}
            System.out.println("Expo Push Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error sending Expo Push Notification to token " + expoPushToken.substring(0,15) + "...: " + e.getMessage());
            // TODO: 예외 처리 (예: 토큰이 유효하지 않은 경우 - DeviceNotRegistered 등)
        }
    }

    // 요청 본문을 위한 내부 클래스 또는 별도 DTO
    // Lombok 사용 시 @Data, @AllArgsConstructor 등으로 간결하게 표현 가능
    static class ExpoPushMessage {
        public String to;
        public String title;
        public String body;
        public String sound;
        public Data data;

        public ExpoPushMessage(String to, String title, String body, String sound, Data data) {
            this.to = to;
            this.title = title;
            this.body = body;
            this.sound = sound;
            this.data = data;
        }

        static class Data {
            public String chatRoomId;
            public Data(String chatRoomId) { this.chatRoomId = chatRoomId; }
        }
    }
}
