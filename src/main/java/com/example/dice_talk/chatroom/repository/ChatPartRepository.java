package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.chatroom.entity.ChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatPartRepository extends JpaRepository<ChatPart, Long> {
    Optional<ChatPart> findFirstByMember_MemberIdOrderByPartIdDesc(long memberId);
    List<ChatPart> findByChatRoom_ChatRoomId(Long chatRoomId);
    Optional<ChatPart> findByChatRoom_ChatRoomIdAndMember_MemberId(Long chatRoomId, Long memberId);
    Optional<ChatPart> findTopByMember_MemberIdAndExitStatusOrderByCreatedAtDesc(
            Long memberId, ChatPart.ExitStatus exitStatus);
    // 특정 채팅방 ID에 있으면서, 특정 ExitStatus를 가진 참여 정보 조회 (예: 현재 참여 중인 사용자만)
    List<ChatPart> findByChatRoom_ChatRoomIdAndExitStatus(Long chatRoomId, ChatPart.ExitStatus exitStatus);
}



