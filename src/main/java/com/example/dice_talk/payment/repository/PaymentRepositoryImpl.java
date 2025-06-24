package com.example.dice_talk.payment.repository;

import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.dashboard.dto.QDailyCountDto;
import com.example.dice_talk.dashboard.dto.TopPayerDto;
import com.example.dice_talk.member.entity.QMember;
import com.example.dice_talk.payment.dto.PaymentAdminResponseDto;
import com.example.dice_talk.payment.entity.Payment;
import com.example.dice_talk.payment.entity.QPayment;
import com.example.dice_talk.product.entity.QProduct;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PaymentAdminResponseDto> findPaymentsForAdmin(String email, String productName,
                                                              Payment.PaymentStatus status,
                                                              LocalDateTime start, LocalDateTime end,
                                                              Pageable pageable) {
        QPayment payment = QPayment.payment;
        QMember member = QMember.member;
        QProduct product = QProduct.product;

        List<PaymentAdminResponseDto> results = queryFactory
                .select(Projections.constructor(PaymentAdminResponseDto.class,
                        payment.orderId,
                        member.email,
                        member.memberId,
                        product.productName,
                        payment.amount,
                        payment.diceAmount,
                        payment.paymentStatus.stringValue(),
                        payment.requestedAt,
                        payment.completedAt
                ))
                .from(payment)
                .join(payment.member, member)
                .join(payment.product, product)
                .where(
                        email != null ? member.email.contains(email) : null,
                        productName != null ? product.productName.contains(productName) : null,
                        status != null ? payment.paymentStatus.eq(status) : null,
                        start != null ? payment.requestedAt.goe(start) : null,
                        end != null ? payment.requestedAt.loe(end) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(payment.requestedAt.desc())
                .fetch();

        Long countResult = queryFactory
                .select(payment.count())
                .from(payment)
                .join(payment.member, member)
                .join(payment.product, product)
                .where(
                        email != null ? member.email.contains(email) : null,
                        productName != null ? product.productName.contains(productName) : null,
                        status != null ? payment.paymentStatus.eq(status) : null,
                        start != null ? payment.requestedAt.goe(start) : null,
                        end != null ? payment.requestedAt.loe(end) : null
                )
                .fetchOne();

        long count = countResult != null ? countResult : 0L;
        return new PageImpl<>(results, pageable, count);
    }


    //웹페이지 : 주간 결제 데이터 조회
    @Override
    public List<DailyCountDto> countPaymentsByDate(LocalDateTime start, LocalDateTime end) {
        QPayment payment = QPayment.payment;

        DateExpression<LocalDate> dateOnly = Expressions.dateTemplate(
                LocalDate.class, "DATE({0})", payment.requestedAt);

//        NumberExpression<Long> countExpr = payment.count();

        return queryFactory
                .select(new QDailyCountDto(dateOnly, payment.count()))
                .from(payment)
                .where(payment.requestedAt.between(start, end)
                        .and(payment.paymentStatus.eq(Payment.PaymentStatus.COMPLETED)))
                .groupBy(dateOnly)
                .orderBy(dateOnly.asc())
                .fetch();
    }

    //웹페이지 : 일일 결제 건수
    @Override
    public int countTodayPayments(LocalDateTime start, LocalDateTime end) {
        QPayment payment = QPayment.payment;

        Long count = queryFactory
                .select(payment.count())
                .from(payment)
                .where(payment.requestedAt.between(start, end)
                        .and(payment.paymentStatus.eq(Payment.PaymentStatus.COMPLETED)))
                .fetchOne();
        return count != null ? count.intValue() : 0;
    }

    //웹페이지 : 결제 총 합
    @Override
    public int sumAmountBetween(LocalDateTime start, LocalDateTime end) {
        QPayment payment = QPayment.payment;

        Integer sum = queryFactory
                .select(payment.amount.sum())
                .from(payment)
                .where(payment.requestedAt.between(start, end)
                        .and(payment.paymentStatus.eq(Payment.PaymentStatus.COMPLETED)))
                .fetchOne();

        return sum != null ? sum : 0;
    }

    //웹페이지 : 결제 많이한 사용자
    @Override
    public List<TopPayerDto> findTopPayersByTotalAmount(int topN) {
        QPayment payment = QPayment.payment;
        QMember member = QMember.member;

        return queryFactory
                .select(Projections.constructor(
                        TopPayerDto.class,
                        member.memberId,
                        member.email,
                        payment.amount.sum().intValue()
                ))
                .from(payment)
                .join(payment.member, member)
                .where(payment.paymentStatus.eq(Payment.PaymentStatus.COMPLETED))
                .groupBy(member.memberId, member.email)
                .orderBy(payment.amount.sum().desc())
                .limit(topN)
                .fetch();
    }
}