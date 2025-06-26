package com.example.dice_talk.chatroom.service;

import com.example.dice_talk.chat.service.ChatService;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.repository.ChatPartRepository;
import com.example.dice_talk.chatroom.repository.ChatRoomRepository;
import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.dashboard.dto.DashboardChatRoom;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.roomevent.entity.RoomEvent;
import com.example.dice_talk.roomevent.repository.RoomEventRepository;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.theme.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class  ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatPartRepository chatPartRepository;
    private final TaskScheduler taskScheduler;
    //종료된 채팅방의 구독 취소 추가
    private final ChatService chatService;
    private final RoomEventRepository roomEventRepository;
    private final MemberService memberService;
    private final ExitLogService exitLogService;
    private final ThemeRepository themeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 키 상수로 추가
    private static final String NICKNAME_POOL_KEY_PREFIX = "nickname:pool";
    private static final String USED_NICKNAME_KEY_PREFIX = "nickname:used";

    private String getNicknamePoolKey(Long chatRoomId, String gender){
        return NICKNAME_POOL_KEY_PREFIX + chatRoomId + ":" + gender;
    }

    private String getUsedNicknameKey(Long chatRoomId, String gender){
        return USED_NICKNAME_KEY_PREFIX + chatRoomId + ":" + gender;
    }

    // 채팅방별 닉네임 풀 초기화
    private void initializeChatRoomNicknamePool(Long chatRoomId){
        String malePoolKey = getNicknamePoolKey(chatRoomId, "male");
        String femalePoolKey = getNicknamePoolKey(chatRoomId, "female");

        // 남성 닉네임 풀 초기화
        if(!redisTemplate.hasKey(malePoolKey)){
            List<String> maleNicknames = List.of(
                    "한가로운 하나", "세침한 세찌", "단호한데 다정한 다오"
            );
            redisTemplate.opsForList().rightPushAll(malePoolKey, maleNicknames.toArray());
        }

        // 여성 닉네임 풀 초기화
        if(!redisTemplate.hasKey(femalePoolKey)){
            List<String> femaleNicknames = List.of(
                    "두 얼굴의 매력 두리", "네모지만 부드러운 네몽", "육감적인 직감파 육댕"
            );
            redisTemplate.opsForList().rightPushAll(femalePoolKey, femaleNicknames.toArray());
        }
    }

    public boolean isMemberPossibleToPart(long memberId) {
        memberService.findVerifiedMember(memberId);
        Optional<ChatPart> lastChatPart = chatPartRepository.findFirstByMember_MemberIdOrderByPartIdDesc(memberId);
        if (lastChatPart.isPresent()) {
            ChatPart chatPart = lastChatPart.get();
            return chatPart.getExitStatus().equals(ChatPart.ExitStatus.MEMBER_EXIT);
        }
        return true;
    }

    // 채팅방 생성 시 'RoomType' 에 따라서 종료시간 설정
    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        if (savedChatRoom.getRoomType().equals(ChatRoom.RoomType.GROUP)) {
            // 채팅방 생성 시 49시간 후 상태 변경 작업 예약
            scheduleDeactivationGroup(savedChatRoom);
            //채팅방 내 사용자 구독 취소
        } else {
            scheduleDeactivationCouple(savedChatRoom);
        }

        return savedChatRoom;
    }

    //49시간 뒤 그룹 채팅방 비활성화, 이벤트 결과 중 성공 시 1대1채팅방 생성 - 테스트용 30분 변경
    private void scheduleDeactivationGroup(ChatRoom chatRoom) {
        LocalDateTime deactivationTime = LocalDateTime.now().plusMinutes(30);
        Date triggerTime = Date.from(deactivationTime.atZone(ZoneId.systemDefault()).toInstant());

        List<RoomEvent> curRoomEvents= chatRoom.getRoomEvents();
        processMutualSelections(curRoomEvents, chatRoom.getTheme().getThemeId());

        taskScheduler.schedule(() -> deactivateChatRoom(chatRoom.getChatRoomId()), triggerTime);
    }

    //24시간 뒤 1:1 채팅방 비활성화 - 테스트용 30분 변경
    private void scheduleDeactivationCouple(ChatRoom chatRoom) {
        LocalDateTime deactivationTime = LocalDateTime.now().plusMinutes(30);
        Date triggerTime = Date.from(deactivationTime.atZone(ZoneId.systemDefault()).toInstant());

        taskScheduler.schedule(() -> deactivateChatRoom(chatRoom.getChatRoomId()), triggerTime);
    }

    @Transactional
    public void deactivateChatRoom(Long chatRoomId) {
        //채팅방 상태 변경
        ChatRoom chatRoom = findVerifiedChatRoom(chatRoomId);
        chatRoom.setRoomStatus(ChatRoom.RoomStatus.ROOM_DEACTIVE);

        // 멤버 상태 업데이트
        List<ChatPart> chatParts = chatPartRepository.findByChatRoom_ChatRoomId(chatRoomId);
        for (ChatPart chatPart : chatParts) {
            chatPart.setExitStatus(ChatPart.ExitStatus.MEMBER_EXIT);
            chatPartRepository.save(chatPart);
        }

        // 채팅방 상태 저장
        chatRoomRepository.save(chatRoom);

        //채팅방의 모든 사용자의 구독 취소
        chatService.unsubscribeAllUsersFromChatRoom(chatRoomId);

        // Redis 에서 닉네임 풀 정리
        cleanupChatRoomNicknamePool(chatRoomId);

        if (chatRoom.getRoomType().equals(ChatRoom.RoomType.GROUP)) {
            List<RoomEvent> roomEvents = roomEventRepository.findAllByChatRoom_ChatRoomId(chatRoomId);

        }
    }

    // Redis에서 닉네임 풀 정리
    private void cleanupChatRoomNicknamePool(Long chatRoomId){
        // 남성 닉네임 풀 정리
        String malePoolKey = getNicknamePoolKey(chatRoomId, "male");
        String maleUsedKey = getUsedNicknameKey(chatRoomId, "male");

        // 여성 닉네임 풀 정리
        String femalePoolKey = getNicknamePoolKey(chatRoomId, "female");
        String femaleUsedKey = getUsedNicknameKey(chatRoomId, "female");

        // Redis에서 해당 채팅방의 닉네임 풀들 삭제
        redisTemplate.delete(malePoolKey);
        redisTemplate.delete(maleUsedKey);
        redisTemplate.delete(femalePoolKey);
        redisTemplate.delete(femaleUsedKey);
    }

    // 특정 채팅방이 존재하는지 확인 후 업데이트
    // 트랜잭션 격리를 `SERIALIZABLE`로 설정 -> 동시성 문제 방지
//    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
//    public ChatRoom updateChatRoom(ChatRoom chatRoom) {
//        ChatRoom findChatRoom = findVerifiedChatRoom(chatRoom.getChatRoomId()); //기존 채팅방 확인
//        Optional.ofNullable(chatRoom.getNotice()).ifPresent(notice -> findChatRoom.setNotice(notice)); //notice 수정
//        return chatRoomRepository.save(findChatRoom);
//    }

    public ChatRoom findChatRoom(long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHATROOM_NOT_FOUND));
    }

    //관리자 조회 (단체, 1대1 채팅방)
    public Page<ChatRoom> findChatRooms(int page, int size) {
        return chatRoomRepository.findAll(PageRequest.of(page, size, Sort.by("chatRoomId").descending()));
    }

    // 내가 참여했던 1대1 채팅방 목록 조회
    public Page<ChatRoom> findMyCoupleChatRooms(int page, int size, long memberId, long loginId) {
        Page<ChatRoom> chatRooms = chatRoomRepository.findAllByMemberIdAndRoomTypeAndRoomStatus(
                memberId, ChatRoom.RoomType.COUPLE, ChatRoom.RoomStatus.ROOM_DEACTIVE, PageRequest.of(page - 1, size, Sort.by("chatRoomId").descending()));
        return chatRooms;
    }

    // 채팅방 삭제시 상태만 종료로 변경
    @Transactional
    public void deleteChatRoom(long chatRoomId) {
        ChatRoom chatRoom = findVerifiedChatRoom(chatRoomId);
        chatRoom.setRoomStatus(ChatRoom.RoomStatus.ROOM_DEACTIVE);
        chatRoom.getChatParts().stream().forEach(chatPart -> chatPart.setExitStatus(ChatPart.ExitStatus.MEMBER_EXIT));
        chatRoomRepository.save(chatRoom);
    }

    // 특정 채팅방에 참여중인 특정 회원의 참여 상태 변경(나가기)
    public void exitChatPart(long chatRoomId, long memberId) {
        Optional<ChatPart> chatPart = chatPartRepository.findByChatRoom_ChatRoomIdAndMember_MemberId(chatRoomId, memberId);
        ChatPart foundChatPart = chatPart.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        if(exitLogService.hasLeftToday(memberId)){
            throw new BusinessLogicException(ExceptionCode.ALREADY_EXITED_TODAY);
        }

        String nickname = foundChatPart.getNickname();

        // 퇴장 메세지 전송 ( 닉네임 있는 경우만 )
        if (nickname != null && !nickname.isEmpty()){
            chatService.sendExitMessage(chatRoomId, nickname);
        }

        // ChatPart 상태 업데이트
        foundChatPart.setExitStatus(ChatPart.ExitStatus.MEMBER_EXIT);
        exitLogService.createExitLog(memberId);
        chatPartRepository.save(foundChatPart);
    }

    // 특정 채팅방에 참여중인 특정 회원의 참여 상태 변경 (아이템 사용 후)
    public void forceExitChatPart(long chatRoomId, long memberId) {
        Optional<ChatPart> chatPart = chatPartRepository.findByChatRoom_ChatRoomIdAndMember_MemberId(chatRoomId, memberId);
        ChatPart foundChatPart = chatPart.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        String nickname = foundChatPart.getNickname();

        // 퇴장 메세지 전송 ( 닉네임 있는 경우만 )
        if (nickname != null && !nickname.isEmpty()){
            chatService.sendExitMessage(chatRoomId, nickname);
        }

        foundChatPart.setExitStatus(ChatPart.ExitStatus.MEMBER_EXIT);
        chatPartRepository.save(foundChatPart);
    }

    public ChatRoom findVerifiedChatRoom(long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHATROOM_NOT_FOUND));
    }

    public ChatRoom createRoomForMatchedUsers(List<Member> members, Long themeId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomType(ChatRoom.RoomType.GROUP);
        chatRoom.setRoomStatus(ChatRoom.RoomStatus.ROOM_ACTIVE);
        Theme theme = new Theme();
        theme.setThemeId(themeId);
        chatRoom.setTheme(theme);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 닉네임 풀 초기화
        initializeChatRoomNicknamePool(savedRoom.getChatRoomId());

        // 성별 분류
        List<ChatPart> maleChatPartList = new ArrayList<>();
        List<ChatPart> femaleChatPartList = new ArrayList<>();

        // chatPart 생성 및 성별 분류
        for (Member member : members) {
            ChatPart chatPart = new ChatPart();
            chatPart.setMember(member);
            chatPart.setChatRoom(savedRoom);
            chatPartRepository.save(chatPart);

            // 성별 분류
            if(member.getGender() == Member.Gender.MALE) {
                maleChatPartList.add(chatPart);
            } else {
                femaleChatPartList.add(chatPart);
            }
        }
        // 자동 닉네임 할당
        assignNicknamesAutomatically(maleChatPartList, femaleChatPartList, savedRoom.getChatRoomId());

        //채팅방 내 사용자 구독 취소
        scheduleDeactivationGroup(savedRoom);

        return savedRoom;
    }

    public ChatRoom createCoupleRoomForMatchedUsers(List<Member> members, Long themeId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomType(ChatRoom.RoomType.COUPLE);
        chatRoom.setRoomStatus(ChatRoom.RoomStatus.ROOM_ACTIVE);
        Theme theme = new Theme();
        theme.setThemeId(themeId);
        chatRoom.setTheme(theme);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        for (Member member : members) {
            ChatPart chatPart = new ChatPart();
            chatPart.setNickname(member.getName());
            chatPart.setMember(member);
            chatPart.setChatRoom(savedRoom);
            chatPartRepository.save(chatPart);
        }

        scheduleDeactivationCouple(savedRoom);

        return savedRoom;
    }

    // RoomEvent 리스트에서 서로 선택한 커플을 찾는 메서드
    public void processMutualSelections(List<RoomEvent> events, Long themeId) {
        // 리스트 복사 (원본 리스트 수정 방지)
        List<RoomEvent> workingList = new ArrayList<>(events);

        // 인덱스 i는 고정하고 j만 증가시키며 비교
        int i = 0;
        while (i < workingList.size()) {
            RoomEvent current = workingList.get(i);
            Long senderId = current.getSenderId();
            Long receiverId = current.getReceiverId();

            boolean foundMatch = false;

            // i 이후의 모든 이벤트와 비교
            for (int j = i + 1; j < workingList.size(); j++) {
                RoomEvent other = workingList.get(j);

                // 상호 선택 확인: current의 sender가 other의 receiver이고
                // current의 receiver가 other의 sender인 경우
                if (senderId == (other.getReceiverId()) &&
                        receiverId.equals(other.getSenderId())) {
                    // 상호 선택 쌍에 대한 작업 수행
                    List<Member> couple = new ArrayList<>();
                    couple.add(memberService.findVerifiedMember(senderId));
                    couple.add(memberService.findVerifiedMember(receiverId));
                    createCoupleRoomForMatchedUsers(couple, themeId);

                    // 매칭된 이벤트 제거 (j 먼저 제거해야 인덱스가 유효함)
                    workingList.remove(j);
                    workingList.remove(i);

                    // 매칭 찾음 표시
                    foundMatch = true;
                    break;
                }
            }

            // 매칭을 찾았으면 i는 그대로 (리스트가 줄어들었으므로)
            // 매칭을 못 찾았으면 i 증가
            if (!foundMatch) {
                i++;
            }
        }

    }

    //웹페이지 : 금일 진행중인 채팅방 조회
    public int countActiveRooms(LocalDateTime start, LocalDateTime end) {
        return chatRoomRepository.countActiveRoomTotal(start,end);
    }
    //웹페이지 : 진행중인 채팅방 조회
    public List<DashboardChatRoom> activeChatRoomCount() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByRoomStatus(ChatRoom.RoomStatus.ROOM_ACTIVE);
        //단체 채팅방
       List<ChatRoom> groupChatRoom = chatRooms.stream().filter(chatRoom -> chatRoom.getRoomType() == ChatRoom.RoomType.GROUP)
               .collect(Collectors.toList());
       //1대1 채팅방
        List<ChatRoom> coupleChatRoom = chatRooms.stream().filter(chatRoom -> chatRoom.getRoomType() == ChatRoom.RoomType.COUPLE)
                .collect(Collectors.toList());

        List<DashboardChatRoom> dashboardChatRooms = new ArrayList<>();
        dashboardChatRooms.add(new DashboardChatRoom(chatRooms.size(), groupChatRoom.size(), coupleChatRoom.size()));
        return dashboardChatRooms;
    }

    //웹페이지 : 주간 진행중인 채팅방
    public List<DailyCountDto> weeklyActiveChatRoom(LocalDateTime start, LocalDateTime end) {
        return chatRoomRepository.countActiveRoomsByDate(start, end);
    }

    // 특정 회원이 참여하고 있는 채팅방과 해당 테마 ID 조회(참여중 없으면 0 반환)
    public Map<String, Long> findCurIds(long memberId){
        memberService.findVerifiedMember(memberId);
        Map<String, Long> curIds = new HashMap<>();
        Long curChatRoomId =  chatPartRepository.findTopByMember_MemberIdAndExitStatusOrderByCreatedAtDesc(memberId, ChatPart.ExitStatus.MEMBER_ENTER)
                .map(chatPart -> chatPart.getChatRoom().getChatRoomId()).orElse(0L);
        curIds.put("curChatRoomId", curChatRoomId);
        if(curChatRoomId == 0L){
            curIds.put("curThemeId", 0L);
            return curIds;
        }
        Long curThemeId = findChatRoom(curChatRoomId).getTheme().getThemeId();
        curIds.put("curThemeId", curThemeId);
        return curIds;
    }

    // 특정 회원이 참여하고 있는 채팅방 조회(참여중 없으면 0 반환)
    public Long findCurChatRoom(long memberId){
        memberService.findVerifiedMember(memberId);
        return chatPartRepository.findTopByMember_MemberIdAndExitStatusOrderByCreatedAtDesc(memberId, ChatPart.ExitStatus.MEMBER_ENTER)
                .map(chatPart -> chatPart.getChatRoom().getChatRoomId()).orElse(0L);
    }

    // 목록조회 로직
    public Page<ChatRoom> searchChatRooms(
            String themeName,
            ChatRoom.RoomStatus roomStatus,
            ChatRoom.RoomType roomType,
            Long chatRoomId,
            String createdAtStart,
            String createdAtEnd,
            int page,
            int size,
            String sort
    ) {
        Long themeId = null;
        if (themeName != null && !themeName.isEmpty()) {
            themeId = themeRepository.findByName(themeName)
                    .map(Theme::getThemeId)
                    .orElse(null);
            if (themeId == null) {
                // 없는 테마면 결과 없음 반환
                return Page.empty();
            }
        }
        Pageable pageable;
        if(sort.equalsIgnoreCase("ASC")){
            pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").ascending());
        } else {
            pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        }
        return chatRoomRepository.searchChatRooms(themeId, roomStatus, roomType, chatRoomId, createdAtStart, createdAtEnd, pageable);
    }

    // 성별별 닉네임 자동 할당 메서드
    private void assignNicknamesAutomatically(List<ChatPart> maleChatParts, List<ChatPart> femaleChatParts, Long chatRoomId) {
        // 남성 닉네임 할당
        assignNicknamesToChatParts(maleChatParts, "male", chatRoomId);

        // 여성 닉네임 할당
        assignNicknamesToChatParts(femaleChatParts, "female", chatRoomId);
    }

    // ChatPart 리스트에 닉네임 할당 메서드
    private void assignNicknamesToChatParts(List<ChatPart> chatParts, String gender, Long chatRoomId) {
        if (chatParts.isEmpty()) {
            return;
        }

        // 성별에 따른 풀 키와 사용 중인 키 생성
        String poolKey = getNicknamePoolKey(chatRoomId, gender);
        String usedKey = getUsedNicknameKey(chatRoomId, gender);

        // Redis에서 사용 가능한 닉네임 가져오기
        List<String> availableNicknames = getAvailableNicknames(chatParts.size(), poolKey, usedKey);

        if (availableNicknames.size() < chatParts.size()) {
            throw new BusinessLogicException(ExceptionCode.NICKNAME_SHORTAGE);
        }

        // 닉네임 할당
        for (int i = 0; i < chatParts.size(); i++) {
            ChatPart chatPart = chatParts.get(i);
            String nickname = availableNicknames.get(i);

            chatPart.setNickname(nickname);
            chatPartRepository.save(chatPart);
        }
    }

    // Redis 에서 사용 가능한 닉네임 가져오는 메서드
    private List<String> getAvailableNicknames(int count, String poolKey, String usedKey) {
        List<String> nicknames = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Object nickname = redisTemplate.opsForList().leftPop(poolKey);
            if (nickname != null) {
                String nick = (String) nickname;
                nicknames.add(nick);
                // 사용 중인 닉네임으로 등록
                redisTemplate.opsForSet().add(usedKey, nick);
            } else {
                break; // 더 이상 사용 가능한 닉네임이 없음
            }
        }

        return nicknames;
    }

//    // 닉네임 배정 후 풀로 반환
//    private void returnNickname(String nickname, Long chatRoomId) {
//        if (nickname != null && !nickname.isEmpty()) {
//            // 닉네임으로 성별 판단
//            String gender = determineGenderByNickname(nickname);
//
//            String usedKey = getUsedNicknameKey(chatRoomId, gender);
//            String poolKey = getNicknamePoolKey(chatRoomId, gender);
//
//            redisTemplate.opsForSet().remove(usedKey, nickname);
//            redisTemplate.opsForList().rightPush(poolKey, nickname);
//        }
//    }
//
//    // 닉네임으로 성별 판단하는 메서드
//    private String determineGenderByNickname(String nickname) {
//        List<String> maleNicknames = List.of("한가로운 하나", "세침한 세찌", "단호하지만 다정한 다오");
//        List<String> femaleNicknames = List.of("호이가 계속되면 두리", "네모지만 부드러운 네몽", "육감적인 직감파 육댕");
//
//        if (maleNicknames.contains(nickname)) {
//            return "male";
//        } else if (femaleNicknames.contains(nickname)) {
//            return "female";
//        } else {
//            // 기본값 (혹시 모를 경우)
//            return "male";
//        }
//    }
}
