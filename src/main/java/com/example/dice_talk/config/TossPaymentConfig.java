package com.example.dice_talk.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TossPaymentConfig {

    @Value("${toss.client-key}")
    private String testClientApiKey;

    @Value("${toss.secret-key}")
    private String testSecretApiKey;

    @Value("${toss.success-url}")
    private String successUrl;

    @Value("${toss.fail-url}")
    private String failUrl;

    public static final String PAYMENT_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    public static final String PAYMENT_CANCEL_URL = "https://api.tosspayments.com/v1/payments";

}

