package com.example.dice_talk.chatroom.service;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.service.ChatService;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.repository.ChatPartRepository;
import com.example.dice_talk.chatroom.repository.ChatRoomRepository;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.theme.sevice.ThemeService;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Service
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatPartRepository chatPartRepository;
    private final TaskScheduler taskScheduler;
    //종료된 채팅방의 구독 취소 추가
    private final ChatService chatService;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatPartRepository chatPartRepository, TaskScheduler taskScheduler, ChatService chatService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatPartRepository = chatPartRepository;
        this.taskScheduler = taskScheduler;
        this.chatService = chatService;
    }

    public boolean isMemberPossibleToPart(long memberId){
        Optional<ChatPart> lastChatPart = chatPartRepository.findFirstByMember_MemberIdOrderByPartIdDesc(memberId);
        if(lastChatPart.isPresent()){
            ChatPart chatPart = lastChatPart.get();
            return chatPart.getExitStatus().equals(ChatPart.ExitStatus.MEMBER_EXIT);
        }
        return true;
    }

    // 채팅방 생성 시 'RoomType' 에 따라서 종료시간 설정
    public ChatRoom createChatRoom(ChatRoom chatRoom){
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        if(savedChatRoom.getRoomType().equals(ChatRoom.RoomType.GROUP)){
        // 채팅방 생성 시 49시간 후 상태 변경 작업 예약
        scheduleDeactivationGroup(savedChatRoom);
        //채팅방 내 사용자 구독 취소
        } else {
            scheduleDeactivationCouple(savedChatRoom);
        }

        return savedChatRoom;
    }

    //49시간 뒤 그룹 채팅방 비활성화
    private void scheduleDeactivationGroup(ChatRoom chatRoom){
        LocalDateTime deactivationTime = LocalDateTime.now().plusHours(49);
        Date triggerTime = Date.from(deactivationTime.atZone(ZoneId.systemDefault()).toInstant());

        taskScheduler.schedule(() -> deactivateChatRoom(chatRoom.getChatRoomId()), triggerTime);
    }

    //24시간 뒤 1:1 채팅방 비활성화
    private void scheduleDeactivationCouple(ChatRoom chatRoom){
        LocalDateTime deactivationTime = LocalDateTime.now().plusHours(24);
        Date triggerTime = Date.from(deactivationTime.atZone(ZoneId.systemDefault()).toInstant());

        taskScheduler.schedule(() -> deactivateChatRoom(chatRoom.getChatRoomId()), triggerTime);
    }
//수정전
//    public void deactivateChatRoom(Long chatRoomId){
//       //채팅방 상태 변경
//        ChatRoom chatRoom = findVerifiedChatRoom(chatRoomId);
//        chatRoom.setRoomStatus(ChatRoom.RoomStatus.ROOM_DEACTIVE);
//
//        // 멤버 상태도 업데이트
//        List<ChatPart> chatParts = chatPartRepository.findByChatRoom_ChatRoomId(chatRoomId);
//        for(ChatPart chatPart : chatParts){
//            chatPart.setExitStatus(ChatPart.ExitStatus.MEMBER_EXIT);
//            chatPartRepository.save(chatPart);
//        }
//
//        // 채팅방 상태 저장
//        chatRoomRepository.save(chatRoom);
//    }

//수정 후
    public void deactivateChatRoom(Long chatRoomId){
        //채팅방 상태 변경
        ChatRoom chatRoom = findVerifiedChatRoom(chatRoomId);
        chatRoom.setRoomStatus(ChatRoom.RoomStatus.ROOM_DEACTIVE);

        // 멤버 상태 업데이트
        List<ChatPart> chatParts = chatPartRepository.findByChatRoom_ChatRoomId(chatRoomId);
        for(ChatPart chatPart : chatParts){
            chatPart.setExitStatus(ChatPart.ExitStatus.MEMBER_EXIT);
            chatPartRepository.save(chatPart);
        }

        // 채팅방 상태 저장
        chatRoomRepository.save(chatRoom);

        //채팅방의 모든 사용자의 구독 취소
        chatService.unsubscribeAllUsersFromChatRoom(chatRoomId);
    }

    // 특정 채팅방이 존재하는지 확인 후 업데이트
    // 트랜잭션 격리를 `SERIALIZABLE`로 설정 -> 동시성 문제 방지
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public ChatRoom updateChatRoom(ChatRoom chatRoom){
        ChatRoom findChatRoom = findVerifiedChatRoom(chatRoom.getChatRoomId()); //기존 채팅방 확인
        Optional.ofNullable(chatRoom.getNotice()).ifPresent(notice -> findChatRoom.setNotice(notice)); //notice 수정
        return chatRoomRepository.save(findChatRoom);
    }

    public ChatRoom findChatRoom(long chatRoomId){
        return chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHATROOM_NOT_FOUND));
    }

    //관리자 조회 (단체, 1대1 채팅방)
    public Page<ChatRoom> findChatRooms(int page, int size){
        return chatRoomRepository.findAll(PageRequest.of(page, size, Sort.by("chatRoomId").descending()));
    }

    // 내가 참여했던 1대1 채팅방 목록 조회
    public Page<ChatRoom> findMyCoupleChatRooms(int page, int size, long memberId, long loginId){
        AuthorizationUtils.isOwner(memberId, loginId);
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

    // 특정 채팅방에 참여중인 특정 회원의 참여 상태 변경
    public void exitChatPart(long chatRoomId, long memberId){
        Optional<ChatPart> chatPart = chatPartRepository.findByChatRoom_ChatRoomIdAndMember_MemberId(chatRoomId, memberId);
        ChatPart foundChatPart = chatPart.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        foundChatPart.setExitStatus(ChatPart.ExitStatus.MEMBER_EXIT);
        chatPartRepository.save(foundChatPart);
    }

    public ChatRoom findVerifiedChatRoom(long chatRoomId){
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHATROOM_NOT_FOUND));
    }
}



//@Service
//@Transactional
//public class ChatRoomService {
//    private final ChatRoomRepository chatRoomRepository;
//    private final ChatRoomQueue chatRoomQueue;
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatRoomQueue chatRoomQueue, SimpMessagingTemplate messagingTemplate) {
//        this.chatRoomRepository = chatRoomRepository;
//        this.chatRoomQueue = chatRoomQueue;
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    public void addToQueue(Long memberId) {
//        chatRoomQueue.addToQueue(memberId);
//        checkAndCreateRoom();
//    }
//
//    private void checkAndCreateRoom() {
//        if (chatRoomQueue.isQueueReady()) {
//            List<Long> members = chatRoomQueue.removeFromQueue();
//            ChatRoom chatRoom = createChatRoom(members);
//            notifyMembers(chatRoom, members);
//        }
//    }
//
//    private ChatRoom createChatRoom(List<Long> members) {
//        ChatRoom chatRoom = new ChatRoom();
//        chatRoom.setRoomType(ChatRoom.RoomType.GROUP);
//        chatRoom.setRoomStatus(ChatRoom.RoomStatus.ROOM_ACTIVE);
//        // 여기에 채팅방 생성 로직 추가 (예: 랜덤 이름 생성)
//        return chatRoomRepository.save(chatRoom);
//    }
//
//    private void notifyMembers(ChatRoom chatRoom, List<Long> members) {
//        for (Long memberId : members) {
//            messagingTemplate.convertAndSend("/sub/chat/queue/" + memberId, chatRoom.getChatRoomId());
//        }
//    }
//
//    // 기존 메소드들...
//}
