package com.example.dice_talk.member.controller;

import com.example.dice_talk.member.mapper.MemberMapper;
import com.example.dice_talk.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper mapper;
}
