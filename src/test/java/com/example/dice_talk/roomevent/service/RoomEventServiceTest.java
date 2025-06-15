package com.example.dice_talk.roomevent.service;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.event.entity.Event;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import com.example.dice_talk.roomevent.repository.RoomEventRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomEventServiceTest {

    @Mock
    private RoomEventRepository roomEventRepository;

    @Mock
    private EventService eventService;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private RoomEventService roomEventService;

    private RoomEvent roomEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(1L);

        Event event = Event.builder().eventId(1L).eventName("시크릿 메시지").build();

        roomEvent = RoomEvent.builder()
                .roomEventId(1L)
                .receiverId(10L)
                .senderId(20L)
                .event(event)
                .chatRoom(chatRoom)
                .build();
    }

    @Test
    @DisplayName("RoomEvent 생성 성공")
    void createRoomEvent() {
        when(memberService.findVerifiedMember(anyLong())).thenReturn(null);
        when(eventService.findVerifiedEvent(anyLong())).thenReturn(roomEvent.getEvent());
        when(chatRoomService.findVerifiedChatRoom(anyLong())).thenReturn(null);
        when(roomEventRepository.save(any())).thenReturn(roomEvent);

        RoomEvent result = roomEventService.createRoomEvent(roomEvent);
        assertNotNull(result);
        assertEquals(roomEvent.getRoomEventId(), result.getRoomEventId());
    }

    @Test
    @DisplayName("RoomEvent 단건 조회 성공")
    void findRoomEvent() {
        when(roomEventRepository.findById(anyLong())).thenReturn(Optional.of(roomEvent));

        RoomEvent result = roomEventService.findRoomEvent(1L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("RoomEvent 단건 조회 실패 - 존재하지 않을 때")
    void findRoomEvent_fail() {
        when(roomEventRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BusinessLogicException.class, () -> roomEventService.findRoomEvent(1L));
    }

    @Test
    @DisplayName("RoomEvent 채팅방 기준 리스트 조회 성공")
    void findRoomEventsByChatRoomId() {
        when(chatRoomService.findVerifiedChatRoom(anyLong())).thenReturn(null);
        when(roomEventRepository.findAllByChatRoom_ChatRoomId(anyLong())).thenReturn(List.of(roomEvent));

        List<RoomEvent> result = roomEventService.findRoomEventsByChatRoomId(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("하트 히스토리 리스트 조회 성공")
    void findRoomEventsByEventAndMemberId() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("roomEventId").descending());
        Page<RoomEvent> page = new PageImpl<>(List.of(roomEvent), pageRequest, 1);

        when(memberService.findVerifiedMember(anyLong())).thenReturn(null);
        when(roomEventRepository.findAllByEvent_EventNameAndReceiverId(eq("시크릿 메시지"), anyLong(), any())).thenReturn(page);

        Page<RoomEvent> result = roomEventService.findRoomEventsByEventAndMemberId(10L, 1, 10);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("RoomEvent 삭제 성공")
    void deleteRoomEvent() {
        try (MockedStatic<AuthorizationUtils> mockedStatic = Mockito.mockStatic(AuthorizationUtils.class)) {

            when(roomEventRepository.findById(anyLong())).thenReturn(Optional.of(roomEvent));

            doNothing().when(roomEventRepository).delete(any());

            roomEventService.deleteRoomEvent(1L, 10L);
            verify(roomEventRepository, times(1)).delete(any());
        }
    }
}