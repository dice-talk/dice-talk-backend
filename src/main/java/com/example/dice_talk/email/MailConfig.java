package com.example.dice_talk.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    //이메일 서버 설정(SMTP 사용, Gmail 기준) + yml에 설정해도 가능
    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com"); // SMTP 서버 설정 (Gmail 기준)
        mailSender.setPort(587);
        mailSender.setUsername("taekho1225@gmail.com");
        mailSender.setPassword("vkczcjqpznybegvz"); // 앱 비밀번호 사용

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return mailSender;
    }
}
