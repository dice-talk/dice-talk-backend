package com.example.dice_talk.chat.service;

import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.repository.ChatRepository;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;

    /*메세지 생성 -> Repository 에 저장,
    DB에 저장된 메세지를 반환하여 클라이언트에 응답
     */
    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }
    //메세지 찾기
    public Chat findChat(long chatId) {
        return chatRepository.findById(chatId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.CHAT_NOT_FOUND));
    }


}
