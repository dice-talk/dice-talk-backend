package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository {
    Optional<Member> findByEmail(String email);
}
