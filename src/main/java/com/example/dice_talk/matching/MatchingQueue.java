package com.example.dice_talk.matching;

import com.example.dice_talk.chat.dto.MatchedResult;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.member.entity.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class MatchingQueue {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomService chatRoomService;
    // 클래스 필드로 추가
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Queue<Member> waitingQueue = new ConcurrentLinkedQueue<>();
    private final Set<Long> waitingMemberIds = new HashSet<>();
    private final int MATCH_SIZE = 2;

    public static final String REDIS_CHANNEL = "match-status";

    public synchronized Optional<MatchedResult> addToQueue(Member member) {
        if (waitingMemberIds.contains(member.getMemberId())) {
            return Optional.empty();
        }

        waitingQueue.offer(member);
        waitingMemberIds.add(member.getMemberId());

        // ✅ JSON 형태로 대기 상태 전송
        Map<String, Object> queueMessage = new HashMap<>();
        queueMessage.put("type", "QUEUE_STATUS");
        queueMessage.put("message", "대기 인원: " + waitingQueue.size());
        queueMessage.put("participants", waitingQueue.stream()
                .map(Member::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        try {
            String json = objectMapper.writeValueAsString(queueMessage);
            redisTemplate.convertAndSend(REDIS_CHANNEL, json); // ✅ 문자열로 전송
        } catch (Exception e) {
            e.printStackTrace(); // or log
        }

        if (waitingQueue.size() >= MATCH_SIZE) {
            List<Member> matched = new ArrayList<>();
            for (int i = 0; i < MATCH_SIZE; i++) {
                Member m = waitingQueue.poll();
                waitingMemberIds.remove(m.getMemberId());
                matched.add(m);
            }

            // ✅ 채팅방 생성
            ChatRoom chatRoom = chatRoomService.createRoomForMatchedUsers(matched);

            // ✅ 매칭 완료 메시지도 JSON으로 전송
            Map<String, Object> matchedMessage = new HashMap<>();
            matchedMessage.put("type", "MATCHED");
            matchedMessage.put("message", "채팅방이 생성되었습니다.");
            matchedMessage.put("chatRoomId", chatRoom.getChatRoomId()); // 실제 생성된 채팅방 ID 넣어주세요

            try {
                String json = objectMapper.writeValueAsString(matchedMessage);
                redisTemplate.convertAndSend(REDIS_CHANNEL, json);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            return Optional.of(matched);
            return Optional.of(new MatchedResult(matched, chatRoom.getChatRoomId()));
        }

        return Optional.empty();
    }

}