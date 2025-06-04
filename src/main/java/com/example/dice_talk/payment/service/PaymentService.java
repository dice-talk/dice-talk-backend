package com.example.dice_talk.payment.service;

import com.example.dice_talk.config.TossPaymentConfig;
import com.example.dice_talk.dicelog.entity.DiceLog;
import com.example.dice_talk.dicelog.service.DiceLogService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.payment.dto.*;
import com.example.dice_talk.payment.entity.Payment;
import com.example.dice_talk.payment.repository.PaymentRepository;
import com.example.dice_talk.product.entity.Product;
import com.example.dice_talk.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MemberService memberService;
    private final TossPaymentConfig tossPaymentConfig;
    private final ProductService productService;
    private final DiceLogService diceLogService;
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    @Transactional
    public Payment requestTossPayment(PaymentRequestDto dto, Long memberId) {
        log.info("결제 요청 시작 - memberId: {}, amount: {}, diceAmount: {}", 
                memberId, dto.getAmount(), dto.getDiceAmount());

        Member member = memberService.findVerifiedMember(memberId);

        // 최소 결제 금액 검증
        if (dto.getAmount() < 1000) {
            log.error("유효하지 않은 결제 금액 - amount: {}", dto.getAmount());
            throw new BusinessLogicException(ExceptionCode.INVALID_PAYMENT_AMOUNT);
        }

        Payment payment = new Payment();
        payment.setAmount(dto.getAmount());
        payment.setDiceAmount(dto.getDiceAmount());
        payment.setOrderId(UUID.randomUUID().toString());
        payment.setPaymentKey(""); // 초기엔 비워두고, 성공 시 업데이트
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setMember(member);

        Product product = productService.findVerifiedProduct(dto.getProductId());
        payment.setProduct(product);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 요청 완료 - orderId: {}", savedPayment.getOrderId());
        
        return savedPayment;
    }

    @Transactional
    public void confirmPayment(TossSuccessDto successDto) {
        log.info("결제 승인 요청 - orderId: {}", successDto.getOrderId());

        // 1. 결제 정보 조회 및 검증
        Payment payment = paymentRepository.findByOrderId(successDto.getOrderId())
                .orElseThrow(() -> {
                    log.error("결제 정보를 찾을 수 없음 - orderId: {}", successDto.getOrderId());
                    return new BusinessLogicException(ExceptionCode.PAYMENT_NOT_FOUND);
                });

        // 결제 상태 검증
        if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
            log.error("이미 완료된 결제 - orderId: {}", successDto.getOrderId());
            throw new BusinessLogicException(ExceptionCode.PAYMENT_ALREADY_COMPLETED);
        }

        if (payment.getPaymentStatus() == Payment.PaymentStatus.FAILED) {
            log.error("이미 실패한 결제 - orderId: {}", successDto.getOrderId());
            throw new BusinessLogicException(ExceptionCode.PAYMENT_ALREADY_FAILED);
        }

        // 결제 시간 검증
        if (ChronoUnit.MINUTES.between(payment.getRequestedAt(), LocalDateTime.now()) > PAYMENT_TIMEOUT_MINUTES) {
            log.error("결제 시간 초과 - orderId: {}, requestedAt: {}", 
                    successDto.getOrderId(), payment.getRequestedAt());
            throw new BusinessLogicException(ExceptionCode.PAYMENT_TIMEOUT);
        }

        // 결제 금액 검증
        if (payment.getAmount() != successDto.getAmount()) {
            log.error("결제 금액 불일치 - orderId: {}, expected: {}, actual: {}", 
                    successDto.getOrderId(), payment.getAmount(), successDto.getAmount());
            throw new BusinessLogicException(ExceptionCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 2. Toss API 호출하여 결제 승인
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossPaymentConfig.getTestSecretApiKey(), "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", successDto.getPaymentKey());
        requestBody.put("orderId", successDto.getOrderId());
        requestBody.put("amount", successDto.getAmount());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                TossPaymentConfig.PAYMENT_CONFIRM_URL,
                request,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            // 3. 결제 성공 -> DB 업데이트
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentKey(successDto.getPaymentKey());
            paymentRepository.save(payment);

            // 4. 다이스 충전 로그 생성
            DiceLog diceLog = new DiceLog();
            diceLog.setQuantity(payment.getDiceAmount());
            diceLog.setInfo("다이스 " + payment.getDiceAmount() + "개 충전");
            diceLog.setMember(payment.getMember());
            diceLog.setProduct(payment.getProduct());
            
            diceLogService.createDiceLogCharge(diceLog, payment.getMember().getMemberId());
            
            log.info("결제 승인 완료 - orderId: {}, amount: {}", 
                    successDto.getOrderId(), successDto.getAmount());
        } else {
            log.error("결제 승인 실패 - orderId: {}, status: {}", 
                    successDto.getOrderId(), response.getStatusCode());
            throw new BusinessLogicException(ExceptionCode.PAYMENT_FAILED);
        }
    }

    @Transactional
    public void failPayment(TossFailDto tossFailDto) {
        log.info("결제 실패 처리 - orderId: {}, message: {}", 
                tossFailDto.getOrderId(), tossFailDto.getMessage());

        Payment payment = paymentRepository.findByOrderId(tossFailDto.getOrderId())
                .orElseThrow(() -> {
                    log.error("결제 정보를 찾을 수 없음 - orderId: {}", tossFailDto.getOrderId());
                    return new BusinessLogicException(ExceptionCode.PAYMENT_NOT_FOUND);
                });

        // 상태 업데이트
        payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        payment.setPaymentKey(tossFailDto.getPaymentKey());
        paymentRepository.save(payment);
        
        log.info("결제 실패 처리 완료 - orderId: {}", tossFailDto.getOrderId());
    }

    @Transactional
    public void cancelPayment(TossCancelDto cancelDto) {
        log.info("결제 취소 요청 - orderId: {}, reason: {}", 
                cancelDto.getOrderId(), cancelDto.getCancelReason());

        Payment payment = paymentRepository.findByOrderId(cancelDto.getOrderId())
                .orElseThrow(() -> {
                    log.error("결제 정보를 찾을 수 없음 - orderId: {}", cancelDto.getOrderId());
                    return new BusinessLogicException(ExceptionCode.PAYMENT_NOT_FOUND);
                });

        payment.setPaymentStatus(Payment.PaymentStatus.CANCELED);
        payment.setRefundReason(cancelDto.getCancelReason());
        paymentRepository.save(payment);
        
        log.info("결제 취소 완료 - orderId: {}", cancelDto.getOrderId());
    }

    @Transactional
    public void refundPayment(String orderId, String reason) {
        log.info("환불 요청 - orderId: {}, reason: {}", orderId, reason);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("결제 정보를 찾을 수 없음 - orderId: {}", orderId);
                    return new BusinessLogicException(ExceptionCode.PAYMENT_NOT_FOUND);
                });

        if (payment.getPaymentStatus() != Payment.PaymentStatus.COMPLETED) {
            log.error("환불 불가능한 결제 상태 - orderId: {}, status: {}", 
                    orderId, payment.getPaymentStatus());
            throw new BusinessLogicException(ExceptionCode.INVALID_REFUND_STATUS);
        }

        // Toss API 호출하여 환불 처리
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossPaymentConfig.getTestSecretApiKey(), "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cancelReason", reason);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                TossPaymentConfig.PAYMENT_CANCEL_URL + "/" + payment.getPaymentKey(),
                request,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
            payment.setRefundReason(reason);
            paymentRepository.save(payment);
            
            log.info("환불 처리 완료 - orderId: {}", orderId);
        } else {
            log.error("환불 처리 실패 - orderId: {}, status: {}", 
                    orderId, response.getStatusCode());
            throw new BusinessLogicException(ExceptionCode.REFUND_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryDto> historyPayment(long memberId) {
        log.info("결제 내역 조회 - memberId: {}", memberId);
        
        List<Payment> payments = paymentRepository.findAllByMember_MemberId(memberId);

        List<PaymentHistoryDto> response = payments.stream()
                .map(payment -> PaymentHistoryDto.builder()
                        .orderId(payment.getOrderId())
                        .amount(payment.getAmount())
                        .diceAmount(payment.getDiceAmount())
                        .paymentStatus(payment.getPaymentStatus())
                        .productImage(payment.getProduct() != null ? payment.getProduct().getProductImage() : null)
                        .productName(payment.getProduct() != null ? payment.getProduct().getProductName() : null)
                        .requestedAt(payment.getRequestedAt())
                        .completedAt(payment.getCompletedAt())
                        .build())
                .collect(Collectors.toList());

        log.info("결제 내역 조회 완료 - memberId: {}, count: {}", memberId, response.size());
        return response;
    }
}