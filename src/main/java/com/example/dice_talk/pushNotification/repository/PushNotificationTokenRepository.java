package com.example.dice_talk.pushNotification.repository;
import com.example.dice_talk.pushNotification.entity.PushNotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PushNotificationTokenRepository extends JpaRepository<PushNotificationToken, Long> {
    // 특정 사용자의 모든 토큰 조회
    List<PushNotificationToken> findByMember_MemberId(Long memberId); // Member 엔티티 내부의 memberId 필드 기준

    // Expo 푸시 토큰 값으로 토큰 정보 조회 (존재 여부 확인 또는 업데이트 시 사용)
    Optional<PushNotificationToken> findByExpoPushToken(String expoPushToken);

    // 특정 사용자의 특정 토큰 값으로 토큰 정보 조회 (더 정확한 확인)
    Optional<PushNotificationToken> findByMember_MemberIdAndExpoPushToken(Long memberId, String expoPushToken);
}
