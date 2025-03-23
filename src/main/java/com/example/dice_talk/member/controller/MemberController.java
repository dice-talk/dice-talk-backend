package com.example.dice_talk.member.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.mapper.MemberMapper;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.member.toss.TossAuthService;
import com.example.dice_talk.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Validated
public class MemberController {
    private final String MEMBER_DEFAULT_URL = "/";
    private final MemberService memberService;
    private final MemberMapper mapper;
    private final TossAuthService tossAuthService;

    @PostMapping("/auth/register")
    public ResponseEntity postMember(@RequestParam String txId,
                                     @RequestParam @Valid MemberDto.Post post){
        //Toss Access Token 발급
        String accessToken = tossAuthService.getAccessToken();
        //txId를 통해 Toss 본인 인증 결과 조회
        Map<String, Object> result = tossAuthService.getVerificationResult(accessToken, txId);

        //본인 인증 후 필요한 정보 추출
        String name = (String) result.get("name");
        String birth= (String) result.get("birthDate");
        Member.Gender gender = (Member.Gender) result.get("gender");
        String ci = (String) result.get("ci");

        //CI 중복 확인
        if (memberService.isCiAlreadyRegistered(ci)) {
            // 이미 등록된 CI일 경우 예외처리
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }

        //Toss 인증에서 가져온 회원 정보
        Member createdMember = memberService.createMember(mapper.memberPostToMember(post),
                name, birth, gender, ci);

        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/notification/{member-id")
    //앱 푸쉬 알림 수신동의 여부 저장
    public ResponseEntity setNotificationConsent(@PathVariable("member-id") @Positive long memberId,
                                                 @Valid  @RequestParam boolean consent){
        memberService.updateNotificationConsent(memberId, consent);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/my-info/{member-id}")
    public ResponseEntity patchMember(@Valid @RequestBody  MemberDto.Patch patch,
                                      @PathVariable("member-id") @Positive long memberId,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){
        //수정할 Member
        patch.setMemberId(memberId);
        Member member = memberService.updateMember(mapper.memberPatchToMember(patch), customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberToMemberResponse(member)), HttpStatus.OK);
    }

    //나의 정보 조회
    @GetMapping("/my-info/{member-id}")
    public ResponseEntity getMember(@PathVariable("member-id") @Positive long memberId,
                                    @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Member member = memberService.findMember(memberId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberToMemberResponse(member)),
                HttpStatus.OK);
    }

    //app 내에서 사용되는 익명 정보 조회
    @GetMapping("/my-page/{member-id}")
    public ResponseEntity getAppMyPage(@PathVariable("member-id") @Positive long memberId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal){

        return new ResponseEntity<>(new SingleResponseDto<>())
    }

    @GetMapping("/office/member-page")
    public ResponseEntity getMembers(@RequestParam("page") @Positive int page,
                                     @RequestParam("size") @Positive int size,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal){

        Page<Member> memberPage = memberService.findMembers(page, size);
        List<Member> members = memberPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.membersToMemberResponses(members), memberPage),
                HttpStatus.OK);
    }

    @DeleteMapping("/my-info/{member-id}")
    public ResponseEntity deleteMember(@PathVariable("member-id") @Positive long memberId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal){

        memberService.deleteMember(memberId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
