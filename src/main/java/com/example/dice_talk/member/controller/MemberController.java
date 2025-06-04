package com.example.dice_talk.member.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.Dto.MemberDto;
import com.example.dice_talk.member.Dto.PasswordChangeDto;
import com.example.dice_talk.member.entity.DeletedMember;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.mapper.MemberMapper;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.member.toss.TossAuthService;
import com.example.dice_talk.report.service.ReportService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Member", description = "회원 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Validated
public class MemberController {
    private final static String MEMBER_DEFAULT_URL = "/";
    private final MemberService memberService;
    private final MemberMapper mapper;
    private final TossAuthService tossAuthService;
    private final ReportService reportService;


    @Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "409", description = "중복 회원",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":409,\"message\":\"Member exists\"}")
                    )
            )
    })
    @PostMapping("/auth/register")
    public ResponseEntity<Void> registerMember(
            @Parameter(
                    description = "회원 가입 요청 DTO", required = true,
                    content = @Content(schema = @Schema(implementation = MemberDto.Post.class))
            )
            @RequestBody @Valid MemberDto.Post postDto) {

        Member tempMember = mapper.memberPostToMember(postDto);
        //로컬 테스트용 임시 Ci 발급
        tempMember.setCi("abcdefghijklmnop" + postDto.getEmail());
        tempMember.setRoles(List.of("USER"));

        Member createdMember = memberService.createMember(tempMember);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());
        return ResponseEntity.created(location).build();
    }

    //관리자 회원가입
    @PostMapping("/admin/register")
    public ResponseEntity<Void> postAdmin (@Parameter(description = "회원 가입 요청 DTO", required = true,
                                            content = @Content(schema = @Schema(implementation = MemberDto.Post.class)))
                                               @RequestBody @Valid MemberDto.Post postDto){

        Member admin = mapper.memberPostToMember(postDto);
        admin.setRoles(List.of("ADMIN"));
        admin.setCi("ADMIN_CI_" + postDto.getEmail());
        Member register = memberService.createMember(admin);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @Operation(summary = "푸시 알림 설정", description = "회원의 푸시 알림 동의 여부를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            )
    })
    @PostMapping("/notification/{member-id}")
    //앱 푸쉬 알림 수신동의 여부 저장
    public ResponseEntity<Void> setNotificationConsent(@Parameter(description = "회원 ID", example = "1") @PathVariable("member-id") @Positive long memberId,
                                                       @Parameter(description = "동의 여부", example = "true") @RequestParam boolean consent,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        if (customPrincipal == null) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED);
        }
        memberService.updateNotificationConsent(memberId, customPrincipal.getMemberId(), consent);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "회원 정보 지역 수정", description = "회원의 지역을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = MemberDto.MyInfoResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @PatchMapping("/my-info/{member-id}")
    public ResponseEntity<SingleResponseDto<MemberDto.MyInfoResponse>> patchMember(@Parameter(description = "수정할 지역") @RequestBody Map<String, String> body,
                                                                                   @Parameter(description = "회원 ID", example = "1") @PathVariable("member-id") @Positive long memberId,
                                                                                   @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        if (customPrincipal == null) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED);
            }
        Member member = memberService.updateMember(body.get("region"), customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberInfoToMemberInfoResponse(member)), HttpStatus.OK);
    }

    // 패스워드 변경
    @Operation(summary = "비밀번호 변경", description = "회원의 비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            )
    })
    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "비밀번호 수정 Dto")
            @Valid @RequestBody PasswordChangeDto passwordChangeDto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        memberService.changePassword(passwordChangeDto, customPrincipal.getMemberId());
        return ResponseEntity.ok().build();
    }

    //나의 정보 조회
    @Operation(summary = "내 정보 조회", description = "본인의 회원 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberDto.MyInfoResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping("/my-info/{member-id}")
    public ResponseEntity<SingleResponseDto<MemberDto.MyInfoResponse>> getMember(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("member-id") @Positive long memberId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Member member = memberService.findMember(memberId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberInfoToMemberInfoResponse(member)),
                HttpStatus.OK);
    }

    //app 내에서 사용되는 익명 정보 조회
    @Operation(summary = "마이 페이지 조회", description = "앱 프로필(익명) 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberDto.MyPageResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping("/my-page/{member-id}")
    public ResponseEntity<SingleResponseDto<MemberDto.MyPageResponse>> getAppMyPage(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("member-id") @Positive long memberId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Member member = memberService.findAppMyPage(memberId, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberToMemberMyPageResponseDto(member)),
                HttpStatus.OK);
    }

    @Operation(summary = "관리자용 회원 목록 조회", description = "전체 회원 정보 목록을 조회합니다.(검색, 정렬, 상태/성별/연령대 조건 필터링)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberDto.MyInfoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping("/admin/member-page")
    public ResponseEntity<MultiResponseDto<MemberDto.MyInfoResponse>> getMembers(
            @Parameter(description = "페이지 번호(1 이상)", example = "1") @RequestParam(value = "page", defaultValue = "1") @Positive int page,
            @Parameter(description = "페이지 크기(1 이상)", example = "10") @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @Parameter(description = "검색어(이름+이메일)") @RequestParam(required = false) String search,
            @Parameter(description = "정렬(예: memberId/asc, name/desc)") @RequestParam(required = false) String sort,
            @Parameter(description = "회원 상태", example = "MEMBER_ACTIVE") @RequestParam(required = false) Member.MemberStatus memberStatus,
            @Parameter(description = "성별", example = "MALE") @RequestParam(required = false) Member.Gender gender,
            @Parameter(description = "연령대", example = "20대") @RequestParam(required = false) String ageGroup) {
        AuthorizationUtils.verifyAdmin();
        Page<Member> memberPage = memberService.findMembersWithConditions(page, size, search, sort, memberStatus, gender, ageGroup);
        List<Member> members = memberPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.membersToMemberResponses(members), memberPage),
                HttpStatus.OK);
    }

    @Operation(summary = "회원 탈퇴", description = "본인 계정을 탈퇴 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            )
    })
    @DeleteMapping("/my-info/{member-id}")
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("member-id") @Positive long memberId,
            @Parameter(description = "탈퇴 사유", example = "사용자 요청") @RequestBody String reason,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        memberService.deleteMember(memberId, customPrincipal.getMemberId(), reason);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "회원 영구 정지", description = "관리자가 특정 회원을 정지합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "정지 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @DeleteMapping("/admin/member-page/{member-id}")
    public ResponseEntity<Void> banMember(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("member-id") @Positive long memberId) {
        AuthorizationUtils.verifyAdmin();
        memberService.banMember(memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "관리자용 탈퇴 회원 목록 조회", description = "탈퇴 회원 정보 목록을 조회합니다. (검색, 정렬, 성별/연령대/탈퇴사유/날짜범위 조건 지정)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberDto.DeletedMemberResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping("/admin/deleted-members")
    public ResponseEntity<MultiResponseDto<MemberDto.DeletedMemberResponse>> getDeletedMembers(
            @Parameter(description = "페이지 번호(1 이상)", example = "1") @RequestParam(value = "page", defaultValue = "1") @Positive int page,
            @Parameter(description = "페이지 크기(1 이상)", example = "10") @RequestParam(value = "size", defaultValue = "10") @Positive int size,
            @Parameter(description = "검색어(이름+이메일)") @RequestParam(required = false) String search,
            @Parameter(description = "성별", example = "MALE") @RequestParam(required = false) Member.Gender gender,
            @Parameter(description = "연령대", example = "20대") @RequestParam(required = false) String ageGroup,
            @Parameter(description = "탈퇴 사유", example = "서비스 이용 불편") @RequestParam(required = false) String reason,
            @Parameter(description = "정렬(예: deletedAt/desc, deletedAt/asc)") @RequestParam(required = false, defaultValue = "deletedAt/desc") String sort,
            @Parameter(description = "탈퇴일 시작(yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) String deletedAtStart,
            @Parameter(description = "탈퇴일 끝(yyyy-MM-dd)", example = "2024-06-01") @RequestParam(required = false) String deletedAtEnd) {
        AuthorizationUtils.verifyAdmin();
        Page<MemberDto.DeletedMemberResponse> responsePage = memberService.findDeletedMembersWithConditions(
                page, size, search, gender, ageGroup, reason, sort, deletedAtStart, deletedAtEnd);
        return new ResponseEntity<>(
                new MultiResponseDto<>(responsePage.getContent(), responsePage),
                HttpStatus.OK
        );
    }

    @Operation(summary = "관리자용 정지 회원 목록 조회", description = "정지 회원 정보 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberDto.BannedMemberResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping("/admin/banned-members")
    public ResponseEntity<MultiResponseDto<MemberDto.BannedMemberResponse>> getBannedMembers(
            @Parameter(description = "페이지 번호(1 이상)", example = "1") @RequestParam(value = "page", defaultValue = "1") @Positive int page,
            @Parameter(description = "페이지 크기(1 이상)", example = "10") @RequestParam(value = "size", defaultValue = "10") @Positive int size) {
        AuthorizationUtils.verifyAdmin();
        Page<Member> bannedMemberPage = memberService.findBannedMembers(page, size);
        List<MemberDto.BannedMemberResponse> responses = bannedMemberPage.getContent().stream()
                .map(member -> {
                    MemberDto.BannedMemberResponse response = new MemberDto.BannedMemberResponse();
                    response.setMemberId(member.getMemberId());
                    response.setEmail(member.getEmail());
                    response.setName(member.getName());
                    response.setReports(reportService.findCompletedReportsByMemberId(member.getMemberId()));
                    return response;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<>(
                new MultiResponseDto<>(responses, bannedMemberPage),
                HttpStatus.OK);
    }

    @Operation(summary = "관리자용 정지 회원 상세 조회", description = "정지 회원 상세 정보와 신고 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberDto.BannedMemberResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping("/admin/banned-members/{member-id}")
    public ResponseEntity<SingleResponseDto<MemberDto.BannedMemberResponse>> getBannedMemberDetail(
            @Parameter(description = "회원 ID", example = "1") @PathVariable("member-id") @Positive long memberId) {
        AuthorizationUtils.verifyAdmin();
        MemberDto.BannedMemberResponse response = memberService.findBannedMemberDetail(memberId);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

}
