package com.example.dice_talk.chatroom.service;

import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.repository.ChatRoomRepository;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.theme.sevice.ThemeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        Optional.ofNullable(chatRoom.getNotice()).ifPresent(notice -> findChatRoom.setNotice(notice));
        Optional.ofNullable(chatRoom.getRoomStatus()).ifPresent(status -> findChatRoom.setRoomStatus(status));
        return chatRoomRepository.save(findChatRoom);
    }

    public ChatRoom findChatRoom(long chatRoomId){
        return chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHATROOM_NOT_FOUND));
    }

    public Page<ChatRoom> findChatRooms(int page, int size){
        return chatRoomRepository.findAll(PageRequest.of(page, size, Sort.by("chatRoomId").descending()));
    }

    // 내가 참여했던 1대1 채팅방 목록 조회
    public Page<ChatRoom> findMyCoupleChatRooms(int page, int size, long memberId){
        Page<ChatRoom> chatRooms = chatRoomRepository.findAllByMemberIdAndRoomType(
                        memberId, ChatRoom.RoomType.COUPLE, PageRequest.of(page-1, size, Sort.by("chatRoomId").descending()));
        return chatRooms;
    }

    // 채팅방 삭제시 상태만 종료로 변경
    @Transactional
    public void deleteChatRoom(long chatRoomId){
        ChatRoom chatRoom = findVerifiedChatRoom(chatRoomId);
        chatRoom.setRoomStatus(ChatRoom.RoomStatus.ROOM_DEACTIVE);
        chatRoom.getChatParts().stream().forEach(chatPart -> chatPart.setExitStatus(ChatPart.ExitStatus.MEMBER_EXIT));
        chatRoomRepository.save(chatRoom);
    }

    public ChatRoom findVerifiedChatRoom(long chatRoomId){
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHATROOM_NOT_FOUND));
    }
}
