package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatPart;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ChatPartRepository extends JpaRepository<ChatPart, Long> {
    Optional<ChatPart> findFirstByMember_MemberIdOrderByPartIdDesc(long memberId);
    List<ChatPart> findByChatRoom_ChatRoomId(Long chatRoomId);
    Optional<ChatPart> findByChatRoom_ChatRoomIdAndMember_MemberId(Long chatRoomId, Long memberId);

}
