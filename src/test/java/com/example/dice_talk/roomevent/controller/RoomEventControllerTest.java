package com.example.dice_talk.roomevent.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.auth.MemberDetailsService;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.roomevent.dto.RoomEventDto;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import com.example.dice_talk.roomevent.mapper.RoomEventMapper;
import com.example.dice_talk.roomevent.mapper.RoomEventMapperImpl;
import com.example.dice_talk.roomevent.service.RoomEventService;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
// BDDMockito
import static org.mockito.BDDMockito.*;
// AssertJ
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoomEventController.class)
@AutoConfigureMockMvc
@Import(RoomEventMapperImpl.class)
@MockBean(JpaMetamodelMappingContext.class)
class RoomEventControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomEventService roomEventService;

    @MockBean
    private EventService eventService;

    @MockBean
    private RoomEventMapper roomEventMapper;

    @MockBean
    private AuthorizationUtils authorizationUtils;

    private final CustomPrincipal customPrincipal = new CustomPrincipal("user@gmail.com", 1L);

    private final RoomEvent roomEvent = RoomEvent.builder()
            .roomEventId(1L)
            .receiverId(1L)
            .senderId(2L)
            .build();

    private final RoomEventDto.Post postDto = new RoomEventDto.Post(1L, 2L, 3L, 4L, "테스트 메시지", RoomEvent.RoomEventType.PICK_MESSAGE);
    private final RoomEventDto.Response responseDto = new RoomEventDto.Response(1L, 2L, 3L, 4L, 5L, "eventName", RoomEvent.RoomEventType.PICK_MESSAGE, LocalDateTime.now(), LocalDateTime.now());

    private static MockedStatic<AuthorizationUtils> mockedStatic;

    @BeforeAll
    static void beforeAll() {
        mockedStatic = org.mockito.Mockito.mockStatic(AuthorizationUtils.class);
    }

    @AfterAll
    static void afterAll() {
        mockedStatic.close();
    }

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customPrincipal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("채팅방 이벤트 등록")
    void postRoomEvent() throws Exception {
        given(roomEventMapper.roomEventPostToRoomEvent(any())).willReturn(roomEvent);
        given(roomEventService.createRoomEvent(any())).willReturn(roomEvent);

        mockMvc.perform(post("/room-event")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("채팅방 이벤트 상세조회")
    void getRoomEvent() throws Exception {
        given(roomEventService.findRoomEvent(anyLong())).willReturn(roomEvent);
        given(roomEventMapper.roomEventToResponse(any())).willReturn(responseDto);

        mockMvc.perform(get("/room-event/{room-event-id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomEventId").value(1L));
    }

    @Test
    @DisplayName("채팅방 이벤트 리스트 조회")
    void getRoomEventsByChatRoom() throws Exception {
        given(roomEventService.findRoomEventsByChatRoomId(anyLong())).willReturn(List.of(roomEvent));
        given(roomEventMapper.roomEventsToResponses(any())).willReturn(List.of(responseDto));

        mockMvc.perform(get("/room-event/chat-room/{chat-room-id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roomEventId").value(1L));
    }

    @Test
    @DisplayName("하트 히스토리 조회")
    void getHeartHistory() throws Exception {
        given(roomEventService.findRoomEventsByEventAndMemberId(anyLong(), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(roomEvent), PageRequest.of(0, 10), 1));
        given(roomEventMapper.roomEventsToResponses(any())).willReturn(List.of(responseDto));

        mockMvc.perform(get("/room-event/history")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roomEventId").value(1L));
    }

    @Test
    @DisplayName("하트 히스토리 삭제")
    void deleteRoomEvent() throws Exception {
        willDoNothing().given(roomEventService).deleteRoomEvent(anyLong(), anyLong());

        mockMvc.perform(delete("/room-event/history/{room-event-id}", 1L).with(csrf()))
                .andExpect(status().isNoContent());
    }
}