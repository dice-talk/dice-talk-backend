package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByCi(String ci);
    //관리자 Web - 조회 로직 : 금일 날짜로 조회
    List<Member> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}
