package com.example.dice_talk.member.repository;

import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByCi(String ci);
    List<Member> findAllByMemberStatus (Member.MemberStatus memberStatus);
    //관리자 Web - 조회 로직 : 금일 날짜로 조회
    List<Member> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    //관리자 web - 주간별 신규 가입자 수 조회
    @Query(value =
            "SELECT DATE(u.created_at) AS dt, COUNT(*) AS cnt " +
                    "FROM users u " +
                    "WHERE u.created_at >= :start " +
                    "  AND u.created_at <  :end " +
                    "GROUP BY DATE(u.created_at) " +
                    "ORDER BY DATE(u.created_at)",
            nativeQuery = true
    )
    List<DailyCountDto> countSignupsByDate(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end
    );

}
