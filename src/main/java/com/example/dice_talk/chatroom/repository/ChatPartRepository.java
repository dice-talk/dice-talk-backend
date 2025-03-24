package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatPart;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatPartRepository extends JpaRepository<ChatPart, Long> {
    @Query("SELECT cp FROM ChatPart cp WHERE cp.member.id = :memberId ORDER BY cp.partId DESC")
    List<ChatPart> findLastChatPart(@Param("memberId") long memberId, Pageable pageable);
}
