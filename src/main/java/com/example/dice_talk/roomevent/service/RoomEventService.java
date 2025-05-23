package com.example.dice_talk.roomevent.service;

import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import com.example.dice_talk.roomevent.repository.RoomEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomEventService {
    private final RoomEventRepository roomEventRepository;
    private final EventService eventService;
    private final ChatRoomService chatRoomService;
    private final MemberService memberService;

    public RoomEventService(RoomEventRepository roomEventRepository, EventService eventService, ChatRoomService chatRoomService, MemberService memberService) {
        this.roomEventRepository = roomEventRepository;
        this.eventService = eventService;
        this.chatRoomService = chatRoomService;
        this.memberService = memberService;
    }

    public RoomEvent createRoomEvent(RoomEvent roomEvent){
        memberService.findVerifiedMember(roomEvent.getReceiverId());
        memberService.findVerifiedMember(roomEvent.getSenderId());
        eventService.findVerifiedEvent(roomEvent.getEvent().getEventId());
        chatRoomService.findVerifiedChatRoom(roomEvent.getChatRoom().getChatRoomId());
        return roomEventRepository.save(roomEvent);
    }

    public RoomEvent findRoomEvent(long roomEventId){
        return findVerifiedEvent(roomEventId);
    }

    public List<RoomEvent> findRoomEventsByChatRoomId(long chatRoomId){
        chatRoomService.findVerifiedChatRoom(chatRoomId);
        return roomEventRepository.findAllByChatRoom_ChatRoomId(chatRoomId);
    }

    public RoomEvent findVerifiedEvent(long eventId){
        return roomEventRepository.findById(eventId).orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.EVENT_NOT_FOUND));
    }

    // 하트 히스토리 찾기 ( RoomEvent 중 이벤트가 Heart With Message 이고, receiverId가 전달받은 memberId인 RoomEventList )
    public Page<RoomEvent> findRoomEventsByEventAndMemberId(long memberId, long loginId){
        if(memberId != loginId){
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
        memberService.findVerifiedMember(memberId);
        return roomEventRepository.findAllByEvent_EventNameAndReceiverId("Heart With Message", memberId,
                PageRequest.of(0, 5, Sort.by("roomEventId").descending()));

    }
}
