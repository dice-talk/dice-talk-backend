package com.example.dice_talk.dashboard.controller;

import com.example.dice_talk.dashboard.dto.MainDashboardResponseDto;
import com.example.dice_talk.dashboard.service.DashboardService;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.response.SwaggerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "adminWe API", description = "이벤트 관련 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashBoardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Admin Web main API", description = "웹 메인페이지 조회, 관리자 페이지의 메인화면을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "관리자 메인페이지 조회 성공",
                            content = @Content(schema = @Schema(implementation = MainDashboardResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping("/dashboard")
    public ResponseEntity<SingleResponseDto<MainDashboardResponseDto>> getMainDashboard() {
        //
        MainDashboardResponseDto dto = dashboardService.findVerifiedExistsDashBoard();
        return new ResponseEntity<>(new SingleResponseDto<>(dto), HttpStatus.OK);
    }

}
