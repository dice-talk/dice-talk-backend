package com.example.dice_talk.member.service;

import com.example.dice_talk.auth.utils.AuthorityUtils;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.MemberRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        //비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPassword);

        //role 초기화
        List<String> roles = authorityUtils.createRoles(member.getEmail());
        member.setRoles(roles);
        //저장
        return memberRepository.save(member);
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
    //단일 조회
    public Member findMember(long memberId, long loginId){
        //로그인한 회원과 동일한지 검증
        AuthorizationUtils.isAdminOrOwner(memberId, loginId);
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

    public void updateNotificationConsent(String email, boolean consent){

        Member member = verifyExistsEmail();l(email);
    }
}

