package com.example.dice_talk.auth.controller;



import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.auth.service.AuthService;
import com.example.dice_talk.response.SwaggerErrorResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "로그아웃", description = "인증된 사용자를 로그아웃 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 로그아웃됨"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}")))}
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> postLogout(@Parameter(hidden = true) Authentication authentication){
        // 인증 객체 유효성 검사
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomPrincipal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401: 로그인 안됨
        }
        // 이메일 추출
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        String username = principal.getEmail();

        //로그아웃
        authService.logout(username);
        return ResponseEntity.ok().build();
    }


    // 토큰 만료시 재발급 API
    @Operation(summary = "Access/Refresh 토큰 재발급", description = "만료된 Access Token과 Refresh Token을 받아 새로운 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = "{\"accessToken\": \"새로운AccessToken\", \"refreshToken\": \"새로운RefreshToken\"}"
                            )
                    )),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"INVALID TOKEN\"}")))}
    )
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @Parameter(description = "만료된 Access Token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String accessTokenHeader,
            @Parameter(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Refresh") String refreshTokenHeader){
        // Bearer 제거
        String accessToken = accessTokenHeader.replace("Bearer ", "");
        String refreshToken = refreshTokenHeader;

        // AuthService에 토큰 재발급 요청
        Map<String, String> tokens = authService.reissueTokens(accessToken, refreshToken);

        return ResponseEntity.ok(tokens);
    }
}

