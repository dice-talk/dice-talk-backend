package com.example.dice_talk.member.service;

import com.example.dice_talk.auth.utils.AuthorityUtils;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.Dto.PasswordChangeDto;
import com.example.dice_talk.member.Dto.ResetPasswordDto;
import com.example.dice_talk.member.entity.DeletedMember;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.repository.DeletedMemberRepository;
import com.example.dice_talk.member.repository.MemberRepository;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.Report;
import com.example.dice_talk.report.repository.ReportRepository;
import com.example.dice_talk.report.service.ReportService;
import com.example.dice_talk.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final DeletedMemberRepository deletedMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityUtils authorityUtils;
    private final ReportRepository reportRepository;

    public Member createMember(Member member) {
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
//        //role 초기화
//        List<String> roles = authorityUtils.createRoles(member.getEmail());
//        member.setRoles(roles);
        //회원가입 직후 0으로 초기화
        member.setTotalDice(0);

        //저장
        return memberRepository.save(member);
    }

    // 주소만 받아서 확인 후 변경
    public Member updateMember(String region, long loginId) {
        Member findMember = findVerifiedMember(loginId);
        if (findMember.getRegion().equals(region)) {
            return findMember;
        } else {
            Optional.ofNullable(region)
                    .ifPresent(value -> findMember.setRegion(value));
            return memberRepository.save(findMember);
        }
    }

    public void changePassword(PasswordChangeDto passwordChangeDto, long memberId) {
        Member findMember = findVerifiedMember(memberId);

        //Sprin Security 의 passwordEncoder 암호 비교용 메서드  (encode는 메번 다른 해시 값 생성)
        boolean isMatch = passwordEncoder.matches(passwordChangeDto.getOldPassword(), findMember.getPassword());
        if (isMatch) {
            Optional.ofNullable(passwordChangeDto.getNewPassword()).ifPresent(newPw->
                    findMember.setPassword(passwordEncoder.encode(newPw)));
            memberRepository.save(findMember);
        } else {
            throw new BusinessLogicException(ExceptionCode.AUTH_INVALID_PASSWORD);
        }
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
        deletedMember.setMemberId(findMember.getMemberId());
        deletedMember.setReason(reason);
        //저장
        deletedMemberRepository.save(deletedMember);
    }

    // 회원 영구정지
    public void banMember(long memberId) {
        Member member = findVerifiedMember(memberId);

        if(member.getMemberStatus() == Member.MemberStatus.MEMBER_BANNED) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }
        member.setMemberStatus(Member.MemberStatus.MEMBER_BANNED);
        memberRepository.save(member);
    }


    //검증로직 : 회원가입 시 이메일 중복 확인 (중복 시 예외처리)
    public void verifyExistsEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }

    }

    // 검증로직 : 이메일 인증 시 존재하는 회원의 이메일인지 확인 (없을 시 예외처리)
    public String findValidEmail(String email){
        Member findMember = memberRepository.findByEmail(email).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND)
        );
        return findMember.getEmail();
    }

    //검증로직 : 등록된 member 조회
    public Member findVerifiedMember(long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        if (member.getMemberStatus().equals(Member.MemberStatus.MEMBER_BANNED)) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
        return member;
    }

    // 검증 로직 : 이메일로 등록된 Member 조회
    public Member findMemberByEmail(String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND)
        );
        return member;
    }

    //검증 로직 : 회원가입 직후에 사용자에게 앱 푸쉬알림 허용 여부 받기
    public void updateNotificationConsent(long memberId, long currentId,  boolean consent) {
        AuthorizationUtils.isOwner(memberId, currentId);
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
    public Member isCifindMember(String ci) {
        Member findMember = memberRepository.findByCi(ci).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        return findMember;
    }

    public void resetPassword(long memberId, ResetPasswordDto resetDto) {
        //이메일로 회원 조회
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        //이중 검증!!
        if (!member.getEmail().equals(resetDto.getEmail())) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED);
        }
        //재설정한 비밀번호 암호화 및 저장 -> 기존 비밀번호는 DB에 덮어씌어짐(따로 삭제X)
        String encryptedPassword = passwordEncoder.encode(resetDto.getNewPassword());
        member.setPassword(encryptedPassword);
        memberRepository.save(member);
    }

    public String findNicknameByMemberId(long memberId) {
        Member member = findVerifiedMember(memberId);
        List<ChatPart> chatParts = member.getChatParts();
        //마지막 참여했던 채팅방에서 nickname 가져오기
        return chatParts.get(chatParts.size() - 1).getNickname();
    }

    // 이벤트 발행을 위한 활동중인 전체 멤버 조회
    public List<Member> findAllActiveMember(){
        return memberRepository.findAllByMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
    }
    //관리자 Web -> 오늘 회원가입한 회원 정보
    public List<String> findTodayRegisteredMembers(){

        LocalDate today = LocalDate.now();
        //오늘 가입한 회원 조회
        List<Member> members = memberRepository.findByCreatedAtBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());    //다음날 00:00시까지

        //조회된 회원의 이름만 출력
        return members.stream()
                .map(member -> member.getName())
                .collect(Collectors.toList());
    }

    public List<DailyCountDto> weeklyNewMember(LocalDateTime start, LocalDateTime end) {
        return memberRepository.countSignupsByDate(start, end);
    }

    //관리자 페이지 요청 여부 확인 (관리자 회원가입 관리)
    public boolean isAdminRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/admin") || request.getHeader("X-Admin-Signup") != null;
    }

    // 탈퇴 회원 목록 조회
    public Page<MemberDto.DeletedMemberResponse> findDeletedMembers(int page, int size) {
        Page<Member> memberPage = memberRepository.findAllDeletedMembers(
                PageRequest.of(page - 1, size, Sort.by("memberId").descending())
        );

        return memberPage.map(member -> {
            DeletedMember deletedMember = deletedMemberRepository.findByMemberId(member.getMemberId())
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

            return MemberDto.DeletedMemberResponse.builder()
                    .memberId(member.getMemberId())
                    .email(member.getEmail())
                    .name(member.getName())
                    .birth(member.getBirth())
                    .region(member.getRegion())
                    .deleteReason(deletedMember.getReason())
                    .deletedAt(deletedMember.getCreatedAt())
                    .build();
        });
    }

    // 정지된 회원 목록 조회
    public Page<Member> findBannedMembers(int page, int size){
        return memberRepository.findAllBannedMembers(PageRequest.of(page - 1, size, Sort.by("memberId").descending()));
    }

    // 정지된 회원 조회를 위한 검증 메서드
    private Member findVerifiedBannedMember(long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        if (member.getMemberStatus() != Member.MemberStatus.MEMBER_BANNED) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        return member;
    }

    public MemberDto.BannedMemberResponse findBannedMemberDetail(long memberId) {
        Member member = findVerifiedBannedMember(memberId);
        List<ReportDto.Response> reports = reportRepository.findAllByMember_MemberIdAndReportStatusWithEmail(memberId, Report.ReportStatus.REPORT_COMPLETED);

        MemberDto.BannedMemberResponse response = new MemberDto.BannedMemberResponse();
        response.setMemberId(member.getMemberId());
        response.setEmail(member.getEmail());
        response.setName(member.getName());
        response.setBirth(member.getBirth());
        response.setRegion(member.getRegion());
        response.setMemberStatus(member.getMemberStatus());
        response.setReports(reports);

        return response;
    }
}
