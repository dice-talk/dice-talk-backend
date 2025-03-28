package com.example.dice_talk.member.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.mapper.MemberMapper;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.member.toss.TossAuthService;
import com.example.dice_talk.utils.AuthorizationUtils;
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
    public ResponseEntity registerMember(@RequestBody @Valid MemberDto.Post postDto) {
        // 회원가입 로직 실행
        // Member createdMember = memberService.createMember(mapper.memberPostToMember(postDto));
        Member tempMember = mapper.memberPostToMember(postDto);

        tempMember.setCi("abcdefghijklmnop" + postDto.getEmail() + postDto.getPhone());

        Member createdMember = memberService.createMember(tempMember);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());
        return ResponseEntity.created(location).build();
    }


    @PostMapping("/notification/{member-id}")
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
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberInfoToMemberInfoResponse(member)), HttpStatus.OK);
    }

    //나의 정보 조회
    @GetMapping("/my-info/{member-id}")
    public ResponseEntity getMember(@PathVariable("member-id") @Positive long memberId,
                                    @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Member member = memberService.findMember(memberId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberInfoToMemberInfoResponse(member)),
                HttpStatus.OK);
    }

    //app 내에서 사용되는 익명 정보 조회
    @GetMapping("/my-page/{member-id}")
    public ResponseEntity getAppMyPage(@PathVariable("member-id") @Positive long memberId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal){

        Member member = memberService.findAppMyPage(memberId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberToMemberMyPageResponseDto(member)),
                HttpStatus.OK);
    }

    @GetMapping("/office/member-page")
    public ResponseEntity getMembers(@RequestParam("page") @Positive int page,
                                     @RequestParam("size") @Positive int size,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal){
        AuthorizationUtils.verifyAdmin();
        Page<Member> memberPage = memberService.findMembers(page, size);
        List<Member> members = memberPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.membersToMemberResponses(members), memberPage),
                HttpStatus.OK);
    }

    @DeleteMapping("/my-info/{member-id}")
    public ResponseEntity deleteMember(@PathVariable("member-id") @Positive long memberId,
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal,
                                       @RequestParam("reason") String reason){

        memberService.deleteMember(memberId, customPrincipal.getMemberId(), reason);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/office/member-page/{member-id}")
    public ResponseEntity banMember(@PathVariable("member-id") @Positive long memberId,
                                    @AuthenticationPrincipal CustomPrincipal customPrincipal){
        AuthorizationUtils.verifyAdmin();
        memberService.banMember(memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
