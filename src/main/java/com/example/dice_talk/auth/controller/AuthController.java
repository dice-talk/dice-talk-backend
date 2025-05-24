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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
            @ApiResponse(responseCode = "403", description = "로그아웃 실패 (권한 없음)",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> postLogout(@Parameter(hidden = true) Authentication authentication){
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        String username = principal.getEmail();

        return authService.logout(username) ?
         new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}

