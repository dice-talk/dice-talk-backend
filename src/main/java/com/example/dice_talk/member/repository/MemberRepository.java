package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByCi(String ci);
    List<Member> findAllByMemberStatus (Member.MemberStatus memberStatus);
}
