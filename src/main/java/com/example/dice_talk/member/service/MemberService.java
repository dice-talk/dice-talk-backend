package com.example.dice_talk.member.service;

import com.example.dice_talk.auth.utils.AuthorityUtils;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.DiceLog;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.MemberRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityUtils authorityUtils;

    public Member createMember(Member member) {
        //중복 검증 -> EmailController 에 구현
//        verifyExistsEmail(member.getEmail());

        //탈퇴한 회원은 6개월이 지나지 않으면 재 회원가입 불가
        //정지된 회원은 회원가입 할 수 없다.


        //비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPassword);

        //role 초기화
        List<String> roles = authorityUtils.createRoles(member.getEmail());
        member.setRoles(roles);
        member.setTotalDice(new ArrayList<DiceLog>);
        //저장
        return memberRepository.save(member);
    }

    //Toss 본인 인증으로 받은 정보 DB에 저장
    public void saveMember(String email, String name, String birth, Member.Gender gender, String ci){
        Member member = new Member();
        member.setEmail(email);
        member.setName(name);
        member.setBirth(birth);
        member.setGender(gender);
        member.setCi(ci);
        //저장
        memberRepository.save(member);
    }

    // CI가 이미 등록되어 있는지 확인하는 메서드
    public boolean isCiAlreadyRegistered(String ci) {
        // MemberRepository의 findByCi 메서드를 호출하여 해당 CI가 존재하는지 확인
            // true (이미 등록된 CI), false (등록되지 않은 CI)
        return memberRepository.findByCi(ci).isPresent();

    }

    public Member updateMember(Member member, long loginId){
        Member findMember = findVerifiedMember(member.getMemberId());
        //권한 확인 : 로그인된 사용자와 동일한지 확인
        AuthorizationUtils.isOwner(member.getMemberId(), loginId);

        //수정 가능한 필드 변경
        Optional.ofNullable(member.getPhone())
                .ifPresent(phone -> findMember.setPhone(phone));
        Optional.ofNullable(member.getPassword())
                .ifPresent(password -> findMember.setPassword(password));
        Optional.ofNullable(member.getRegion())
                .ifPresent(region -> findMember.setRegion(region));
        Optional.ofNullable(member.isNotification())
                .ifPresent(notification -> findMember.setNotification(notification));
        //저장
        return memberRepository.save(findMember);
    }

    //단일 조회(my-info/{member-id})
    public Member findMember(long memberId, long loginId){
        //로그인한 회원과 동일한지 검증
        AuthorizationUtils.isAdminOrOwner(memberId, loginId);
        return findVerifiedMember(memberId);
    }

    //AppMyPage(앱프로필-익명) 단일 조회 (my-page/{member-id})
    public ChatPart findAppMyPage(long memberId, long loginId){
       //회원 본인만 조회 가능 : 로그인한 회원과 동일한지 검증
        AuthorizationUtils.isOwner(memberId, loginId);
        return findVerifiedMember(memberId);
    }

    //전체조회(관리자)
    public Page<Member> findMembers(int page, int size){
        // page 번호 검증
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        // Page객체에 담아서 반환(내림차순)
        return memberRepository.findAll(PageRequest.of(page-1, size, Sort.by("memberId").descending()));
    }

    //회원 탈퇴
    public void deleteMember(long memberId, long loginId) {
        //로그인한 사용자와 동일한지(관리자, 해당 사용자 권한)
        AuthorizationUtils.isAdminOrOwner(memberId, loginId);
        //DB에서 회원 조회
        Member findMember = findVerifiedMember(memberId);
        //회원 상태 변경 - MEMBER_DELETED || MEMBER_BANNED : 예외발생
        if(findMember.getMemberStatus() == Member.MemberStatus.MEMBER_DELETED
                || findMember.getMemberStatus() == Member.MemberStatus.MEMBER_BANNED){
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        //탈퇴상태로 변경
        findMember.deactivate();
        //저장
        memberRepository.save(findMember);

    }


    //검증로직 : 회원가입 시 이메일 중복 확인
    public void verifyExistsEmail(String email){
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if(findMember.isPresent()){
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }

    }

    //검증로직 : 등록된 member 조회
    public Member findVerifiedMember(long memberId){
        return memberRepository.findById(memberId).orElseThrow(
                ()-> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    //검증 로직 : 회원가입 직후에 사용자에게 앱 푸쉬알림 허용 여부 받기
    public void updateNotificationConsent(long memberId, boolean consent){
        Member findMember = findVerifiedMember(memberId);

            findMember.setNotification(consent);
            //저장
            memberRepository.save(findMember);
    }

    //검증로직 : 회원 탈퇴 후 6개월 이하인 경우 회원가입 불가(예외처리) + CI로 확인 해야해
    //검증로직 : MEMBER_BANNED 상태인 회원은 탈퇴할 수 없다.
}

