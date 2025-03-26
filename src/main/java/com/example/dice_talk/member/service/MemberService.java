package com.example.dice_talk.member.service;

import com.example.dice_talk.auth.utils.AuthorityUtils;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.DeletedMember;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.DeletedMemberRepository;
import com.example.dice_talk.member.repository.MemberRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final DeletedMemberRepository deletedMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityUtils authorityUtils;


    public Member createMember(Member member){
//        CI 중복 확인
        if (isCiAlreadyRegistered(member.getCi())) {
            // 이미 등록된 CI일 경우 예외처리
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }
        //탈퇴한 회원은 6개월이 지나지 않았거나, 정지된 회원이라면 회원가입 할 수 없다.
        verifyEligibilityForRegistration(member.getCi());
        //비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPassword);
        //role 초기화
        List<String> roles = authorityUtils.createRoles(member.getEmail());
        member.setRoles(roles);
        //회원가입 직후 0으로 초기화
        member.setTotalDice(0);

        //저장
        return memberRepository.save(member);
    }

    public Member updateMember(Member member, long loginId) {
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
    public Member findMember(long memberId, long loginId) {
        //로그인한 회원과 동일한지 검증
        AuthorizationUtils.isAdminOrOwner(memberId, loginId);
        return findVerifiedMember(memberId);
    }

    //AppMyPage(앱프로필-익명) 단일 조회 (my-page/{member-id})
    public Member findAppMyPage(long memberId, long loginId) {
        //회원 본인만 조회 가능 : 로그인한 회원과 동일한지 검증
        AuthorizationUtils.isOwner(memberId, loginId);
        return findVerifiedMember(memberId);
    }

    //전체조회(관리자)
    public Page<Member> findMembers(int page, int size) {
        // page 번호 검증
        if (page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        // Page객체에 담아서 반환(내림차순)
        return memberRepository.findAll(PageRequest.of(page - 1, size, Sort.by("memberId").descending()));
    }

    //회원 탈퇴
    public void deleteMember(long memberId, long loginId, String reason) {
        //로그인한 사용자와 동일한지(관리자, 해당 사용자 권한)
        AuthorizationUtils.isAdminOrOwner(memberId, loginId);
        //DB에서 회원 조회
        Member findMember = findVerifiedMember(memberId);
        //이미 탈퇴한 회원 또는 정지된 회원은 예외 발생
        ineligibleMember(findMember);
        //탈퇴상태로 변경
        findMember.deactivate();
        //저장
        memberRepository.save(findMember);

        //탈퇴 정보 저장
        DeletedMember deletedMember = new DeletedMember();
        deletedMember.setDMemberId(findMember.getMemberId());
        deletedMember.setReason(reason);
        //저장
        deletedMemberRepository.save(deletedMember);
    }

    // 회원 영구정지
    public void banMember(long memberId){
        Member member = findVerifiedMember(memberId);
        member.setMemberStatus(Member.MemberStatus.MEMBER_BANNED);
        memberRepository.save(member);
    }


    //검증로직 : 회원가입 시 이메일 중복 확인
    public void verifyExistsEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }

    }

    //검증로직 : 등록된 member 조회
    public Member findVerifiedMember(long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        if(member.getMemberStatus().equals(Member.MemberStatus.MEMBER_BANNED)){
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
        return member;
    }

    //검증 로직 : 회원가입 직후에 사용자에게 앱 푸쉬알림 허용 여부 받기
    public void updateNotificationConsent(long memberId, boolean consent) {
        Member findMember = findVerifiedMember(memberId);

        findMember.setNotification(consent);
        //저장
        memberRepository.save(findMember);
    }

    //이미 탈퇴한 회원이거나, 정지된 회원이라면 예외발생
    public void ineligibleMember(Member member) {

        if (member.getMemberStatus() == Member.MemberStatus.MEMBER_DELETED
                || member.getMemberStatus() == Member.MemberStatus.MEMBER_BANNED) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
    }

    //탈퇴회원, 정지회원에 대한 로직
    private void verifyEligibilityForRegistration(String ci) {
        //ci 로 회원 찾기
        Optional<Member> findMember = memberRepository.findByCi(ci);
        //DB에 등록된 회원이 있다면,
        if (findMember.isPresent()) {
            Member member = findMember.get();
            //탈퇴한 회원인지 확인
            if (member.getMemberStatus() == Member.MemberStatus.MEMBER_DELETED) {
                DeletedMember deletedMember = deletedMemberRepository.findByMemberId(member.getMemberId()).orElseThrow();

                //탈퇴 후 6개월이 지나지 않았다면 회원가입 불가
                if (LocalDateTime.now().isBefore(deletedMember.getCreatedAt().plusMonths(6))) {
                    throw new IllegalStateException("탈퇴 후 6개월 이내에는 재가입할 수 없습니다.");
                }
            }
            //정지된 회원(MEMBER_BANNED)은 회원가입 불가
            if (member.getMemberStatus() == Member.MemberStatus.MEMBER_BANNED) {
                throw new IllegalStateException("정지된 회원은 가입할 수 없습니다.");
            }
        }
    }
    // CI가 이미 등록되어 있는지 확인하는 메서드
    public boolean isCiAlreadyRegistered(String ci) {
        // MemberRepository의 findByCi 메서드를 호출하여 해당 CI가 존재하는지 확인
        // true (이미 등록된 CI), false (등록되지 않은 CI)
        return memberRepository.findByCi(ci).isPresent();
    }

    //Ci를 통해 등록된 회원인지 확인, 없다면 404
    public Member isCifindMember(String ci){
        Member findMember = memberRepository.findByCi(ci).orElseThrow(
                ()-> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        return findMember;
    }

    public void resetPassword (String email, String newPassword){
        //이메일로 회원 조회
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        //재설정한 비밀번호 암호화 및 저장 -> 기존 비밀번호는 DB에 덮어씌어짐(따로 삭제X)
        String encryptedPassword = passwordEncoder.encode(newPassword);
        member.setPassword(encryptedPassword);
        memberRepository.save(member);
    }

}
