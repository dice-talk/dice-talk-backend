package com.example.dice_talk.payment.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.product.entity.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long paymentId;

    @Column(nullable = false)
    private int amount; // 결제 금액

    @Column(nullable = false)
    private int diceAmount; // 해당 결제로 지급된 다이스 양 (ex. 10다이스)

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime requestedAt; // 결제요청시간

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime completedAt; // 승인/거절처리시간

    @Column
    private String refundReason; // 환불 사유

    @Column(nullable = false)
    private String paymentKey;

    @Column(nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public enum PaymentStatus {
        PENDING("결제 대기"),
        COMPLETED("결제 완료"),
        FAILED("결제 실패"),
        CANCELED("결제 취소"),
        REFUNDED("결제 환불");

        @Getter
        private String status;

        PaymentStatus(String status) {
            this.status = status;
        }
    }
}
