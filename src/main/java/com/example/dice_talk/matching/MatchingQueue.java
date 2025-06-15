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
//    private final Set<Long> waitingMemberIds = new HashSet<>();
    private final Map<String, Queue<Member>> waitingQueues = new HashMap<>();
    private final Map<String, Set<Long>> waitingMemberIds = new HashMap<>();
    private final int MATCH_SIZE = 6;

    public static final String REDIS_CHANNEL = "match-status";

    public synchronized Optional<MatchedResult> addToQueue(Member member, Long themeId, String region, String ageGroup) {
        String key = themeId + "::" + region + "::" + ageGroup;

        waitingQueues.putIfAbsent(key, new ConcurrentLinkedQueue<>());
        waitingMemberIds.putIfAbsent(key, new HashSet<>());

        Queue<Member> queue = waitingQueues.get(key);
        Set<Long> memberIds = waitingMemberIds.get(key);

        if (memberIds.contains(member.getMemberId())) {
            return Optional.empty();
        }

        queue.offer(member);
        memberIds.add(member.getMemberId());

        // 대기 상태 전송
        sendStatusToRedis(queue, key);

        if (queue.size() >= MATCH_SIZE) {
            List<Member> matched = new ArrayList<>();
            for (int i = 0; i < MATCH_SIZE; i++) {
                Member m = queue.poll();
                memberIds.remove(m.getMemberId());
                matched.add(m);
            }

            if (queue.isEmpty()) {
                waitingQueues.remove(key);
                waitingMemberIds.remove(key);
            }

            ChatRoom chatRoom = chatRoomService.createRoomForMatchedUsers(matched, themeId);
            sendMatchedMessageToRedis(chatRoom.getChatRoomId());

            return Optional.of(new MatchedResult(matched, chatRoom.getChatRoomId()));
        }

        return Optional.empty();
    }

    private void sendStatusToRedis(Queue<Member> queue, String key) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "QUEUE_STATUS");
        message.put("key", key);
        message.put("message", "대기 인원: " + queue.size());
        message.put("participants", queue.stream()
                .map(Member::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        try {
            redisTemplate.convertAndSend(REDIS_CHANNEL, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMatchedMessageToRedis(Long chatRoomId) {
        Map<String, Object> matchedMessage = new HashMap<>();
        matchedMessage.put("type", "MATCHED");
        matchedMessage.put("message", "채팅방이 생성되었습니다.");
        matchedMessage.put("chatRoomId", chatRoomId);
        try {
            redisTemplate.convertAndSend(REDIS_CHANNEL, objectMapper.writeValueAsString(matchedMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}