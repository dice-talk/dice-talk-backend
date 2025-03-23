package com.example.dice_talk.chatroom.service;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.repository.ChatRoomRepository;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.theme.sevice.ThemeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    public ChatRoom createChatRoom(ChatRoom chatRoom){
        return chatRoomRepository.save(chatRoom);
    }

    // 특정 채팅방이 존재하는지 확인 후 업데이트
    // 트랜잭션 격리를 `SERIALIZABLE`로 설정 -> 동시성 문제 방지
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public ChatRoom updateChatRoom(ChatRoom chatRoom){
        ChatRoom findChatRoom = findVerifiedChatRoom(chatRoom.getChatRoomId());
    }

    public ChatRoom findVerifiedChatRoom(long chatRoomId){
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHATROOM_NOT_FOUND));
    }
}
