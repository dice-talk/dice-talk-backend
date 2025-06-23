package com.example.dice_talk.member.repository;

import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByCi(String ci);
    List<Member> findAllByMemberStatus (Member.MemberStatus memberStatus);

    @Query("SELECT m FROM Member m WHERE m.memberStatus = 'MEMBER_DELETED'")
    Page<Member> findAllDeletedMembers(Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.memberStatus = 'MEMBER_BANNED'")
    Page<Member> findAllBannedMembers(Pageable pageable);
}
