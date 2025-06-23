package com.example.dice_talk.payment.repository;

import com.example.dice_talk.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByPaymentKey(String paymentKey);
    List<Payment> findAllByMember_MemberId(Long memberId);

}

