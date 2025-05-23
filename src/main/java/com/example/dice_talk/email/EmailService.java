package com.example.dice_talk.email;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {
 //이메일 인증 로직
    private final JavaMailSender mailSender; //스프링에서 제공하는 이메일 전송을 위한 API -> MailSender 인터페이스 확장
    private final RedisTemplate<String, String> redisTemplate;
    private static final long EXPIRATION_TIME = 5; // 인증번호 유효 시간 (5분)

    public void sendVerificationCode(String email) {
        String verificationCode = generateVerificationCode();

        // Redis에 저장 (5분간 유지)
        redisTemplate.opsForValue().set(email, verificationCode, EXPIRATION_TIME, TimeUnit.MINUTES);

        // 이메일 전송
        sendEmail(email, verificationCode);
    }
    //Redis에서 인증번호 비교 후 검증
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(email);

        if (storedCode == null || !storedCode.equals(code)) {
            return false;
        }

        // 인증 완료 후 Redis에서 삭제 (선택 사항)
        redisTemplate.delete(email);
        return true;
    }
    //랜덤 6자리 인증번호 생성
    private String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6자리 랜덤 숫자
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("이메일 인증 코드");
        message.setText("인증 코드: " + code);
        mailSender.send(message);
    }
}
