package com.example.dice_talk.member.repository;

import com.example.dice_talk.member.entity.DeletedMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeletedMemberRepository extends JpaRepository<DeletedMember, Long> {
    Optional<DeletedMember> findByMemberId(long memberId);
    Page<DeletedMember> findAll (Pageable pageable);
}
