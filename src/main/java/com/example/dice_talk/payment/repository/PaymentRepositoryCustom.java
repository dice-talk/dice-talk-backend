package com.example.dice_talk.payment.repository;

import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.dashboard.dto.TopPayerDto;
import com.example.dice_talk.payment.dto.PaymentAdminResponseDto;
import com.example.dice_talk.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepositoryCustom {
    Page<PaymentAdminResponseDto> findPaymentsForAdmin(String email, String productName,
                                                       Payment.PaymentStatus status,
                                                       LocalDateTime start, LocalDateTime end,
                                                       Pageable pageable);
    //웹페이지 : 주간 데이터 조회
    List<DailyCountDto> countPaymentsByDate(LocalDateTime start, LocalDateTime end);
    //웹페이지
    int countTodayPayments(LocalDateTime start, LocalDateTime end);
    int sumAmountBetween(LocalDateTime start, LocalDateTime end);
    List<TopPayerDto> findTopPayersByTotalAmount(int topN);
}

