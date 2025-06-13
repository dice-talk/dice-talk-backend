package com.example.dice_talk.pushNotification.entity;

import com.example.dice_talk.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "push_notification_token", uniqueConstraints = {
        @UniqueConstraint(columnNames = "expoPushToken") // expoPushToken 값은 유일하도록 설정
})
public class PushNotificationToken {

    // 필요한 생성자, getter, setter ...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 사용
    @JoinColumn(name = "member_id", nullable = false) // Member 테이블의 PK를 참조
    private Member member; // Member 엔티티 (미리 정의되어 있어야 함)

    @Column(nullable = false, unique = true)
    private String expoPushToken;

    @Column
    private String deviceType; // 예: "ANDROID", "IOS"

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist // 엔티티가 처음 저장될 때 호출
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate // 엔티티가 업데이트될 때 호출
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
