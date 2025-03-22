package com.example.dice_talk.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisRepositoryConfig {
    // yml 파일에서 Redis 서버의 호스트 주소를 가져온다.
    @Value("${spring.data.redis.host}")
    private String host;

    // yml 파일에서 Redis 서버의 포트를 가져온다.
    @Value("${spring.data.redis.port}")
    private int port;

    /*RedisConnectionFactory 빈을 생성하는 메서드
    LettuceConnectionFactory는 Redis와의 연결을 비동기적으로 관리하는 클라이언트 라이브러리.
    비동기, 동시성 및 스레드 안정성을 지원하며, 다수의 Redis 명령어를 효율적으로 처리할 수 있다.
    * */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        // RedisStandaloneConfiguration 객체 생성하여 Redis 서버의 호스트와 포트 설정
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host); // Redis 서버의 호스트 설정
        redisStandaloneConfiguration.setPort(port); // Redis 서버의 포트 설정

        // LettuceConnectionFactory를 사용하여 Redis 연결을 설정
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        return lettuceConnectionFactory;
    }

    /*RedisTemplate 빈을 생성하는 메서드
    * Redis 서버와 데이터를 읽고 쓰기 위한 주요 인터페이스
    * 모든 자료구조에 대해 다양한 작업을 수행할 수 있는 방법 제공.*/
    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        // 객체 생성
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // RedisConnectionFactory를 RedisTemplate에 설정
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Redis의 키와 값을 직렬화
        // Redis에 데이터를 저장할 때 직렬화 방식을 지정, 데이터 저장 형식을 정의한다.
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
