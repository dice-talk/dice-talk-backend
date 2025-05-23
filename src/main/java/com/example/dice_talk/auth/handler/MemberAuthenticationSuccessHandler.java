package com.example.dice_talk.auth.handler;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.auth.MemberDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

// 로그인 인증 성공 시 추가 작업
@Slf4j
public class MemberAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        MemberDetailsService.MemberDetails memberDetails = (MemberDetailsService.MemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.getMemberId();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = new ObjectMapper().writeValueAsString(Map.of("memberId", memberId));
        response.getWriter().write(json);

    }
}
