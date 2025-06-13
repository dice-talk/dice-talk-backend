package com.example.dice_talk.auth.handler;

import com.example.dice_talk.response.ErrorResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 로그인 인증 실패 시 추가 작업
@Slf4j
public class MemberAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("# Authentication failed: {}", exception.getMessage());

        if (exception instanceof DisabledException || request.getAttribute("exception").equals("BANNED")){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403, \"message\":\"정지된 회원입니다.\"}");
        } else {
        // sendErrorResponse() 메서드를 호출해 출력 스트림에 Error 정보 담음.
        sendErrorResponse(response);
        }
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        // JSON 문자열로 변환하기 위해 Gson 객체 생성
        Gson gson = new Gson();
        //  ErrorResponse 객체를 생성, ErrorResponse.of() 메서드로 HttpStatus.UNAUTHORIZED 상태 코드를 전달
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED);
        // response의 Content Type이 json 이라는 것을 클라이언트에게 알려줄 수 있도록 HTTP Header에 추가
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // response의 status가 401임을 알려줌.
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // Gson으로 ErrorResponse 를 JSON 포맷 문자열로 변환 후 출력 스트림 생성
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }
}
