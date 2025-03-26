package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatPart;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatPartRepository extends JpaRepository<ChatPart, Long> {
    Optional<ChatPart> findFirstByMember_MemberIdOrderByPartIdDesc(long memberId);
}
