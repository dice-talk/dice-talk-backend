package com.example.dice_talk.Home.controller;

import com.example.dice_talk.Home.dto.HomeResponseDto;
import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.mapper.NoticeMapper;
import com.example.dice_talk.notice.service.NoticeService;
import com.example.dice_talk.theme.dto.ThemeDto;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.theme.mapper.ThemeMapper;
import com.example.dice_talk.theme.sevice.ThemeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class HomeController {
    private final ThemeService themeService;
    private final NoticeService noticeService;
    private final MemberService memberService;
    private final ThemeMapper themeMapper;
    private final NoticeMapper noticeMapper;

    public HomeController(ThemeService themeService, NoticeService noticeService, MemberService memberService, ThemeMapper themeMapper, NoticeMapper noticeMapper) {
        this.themeService = themeService;
        this.noticeService = noticeService;
        this.memberService = memberService;
        this.themeMapper = themeMapper;
        this.noticeMapper = noticeMapper;
    }

    @GetMapping("/home")
    public ResponseEntity getHome(@AuthenticationPrincipal CustomPrincipal customPrincipal){
        Member member = memberService.findVerifiedMember(customPrincipal.getMemberId());
        List<ThemeDto.Response> themes = themeMapper.themesToThemeResponses(themeService.findAllThemesOn());
        List<NoticeDto.Response> notices = noticeMapper.noticesToNoticeResponses(noticeService.findBannerEvents());
        return new ResponseEntity(new HomeResponseDto(themes, notices), HttpStatus.OK);
    }
}
