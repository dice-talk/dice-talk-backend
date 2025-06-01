package com.example.dice_talk.Home.controller;

import com.example.dice_talk.Home.dto.HomeResponseDto;
import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chatroom.dto.ChatRoomDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.mapper.NoticeMapper;
import com.example.dice_talk.notice.service.NoticeService;
import com.example.dice_talk.notification.service.NotificationService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.theme.dto.ThemeDto;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.theme.mapper.ThemeMapper;
import com.example.dice_talk.theme.sevice.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "HOME GET API", description = "홈 화면 조회 API")
@SecurityRequirement(name = "JWT")
@RestController
public class HomeController {
    private final ThemeService themeService;
    private final NoticeService noticeService;
    private final MemberService memberService;
    private final ThemeMapper themeMapper;
    private final NoticeMapper noticeMapper;
    private final NotificationService notificationService;

    public HomeController(ThemeService themeService, NoticeService noticeService, MemberService memberService, ThemeMapper themeMapper, NoticeMapper noticeMapper, NotificationService notificationService) {
        this.themeService = themeService;
        this.noticeService = noticeService;
        this.memberService = memberService;
        this.themeMapper = themeMapper;
        this.noticeMapper = noticeMapper;
        this.notificationService = notificationService;
    }

    @Operation(summary = "어플리케이션 HOME 조회", description = "로그인한 사용자가 어플리케이션 홈 회면을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = HomeResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping("/home")
    public ResponseEntity<HomeResponseDto> getHome(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Member member = memberService.findVerifiedMember(customPrincipal.getMemberId());
        List<ThemeDto.Response> themes = themeMapper.themesToThemeResponses(themeService.findAllThemesNotClose());
        List<NoticeDto.Response> notices = noticeMapper.noticesToNoticeResponses(noticeService.findBannerEvents());
        boolean hasNewNotifications = notificationService.countUnread(customPrincipal.getMemberId()) > 0;
        return new ResponseEntity<>(new HomeResponseDto(themes, notices, hasNewNotifications), HttpStatus.OK);
    }
}
