package com.example.dice_talk.dicelog.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chatroom.dto.ChatRoomDto;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.dicelog.dto.DiceLogDto;
import com.example.dice_talk.dicelog.entity.DiceLog;
import com.example.dice_talk.dicelog.mapper.DiceLogMapper;
import com.example.dice_talk.dicelog.service.DiceLogService;
import com.example.dice_talk.response.ErrorResponse;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dices")
public class DiceLogController {
    private final DiceLogService diceLogService;
    private final DiceLogMapper mapper;

    public DiceLogController(DiceLogService diceLogService, DiceLogMapper mapper) {
        this.diceLogService = diceLogService;
        this.mapper = mapper;
    }

    @Operation(summary = "다이스 충전 로그", description = "다이스 충전 로그를 등록합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "다이스 충전 성공",
                            content = @Content(schema = @Schema(implementation = DiceLogDto.Response.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}")))}
    )
    @PostMapping("/charge")
    public ResponseEntity<SingleResponseDto<DiceLogDto.Response>> postChargeLog(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "충전 로그 생성 요청 본문",
                                                                                        required = true, content = @Content(schema = @Schema(implementation = DiceLogDto.Post.class)))
                                                                                @RequestBody DiceLogDto.Post dto,
                                                                                @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        dto.setMemberId(customPrincipal.getMemberId());
        DiceLog diceLog = mapper.diceLogPostToDiceLog(dto);
        DiceLog createdLog = diceLogService.createDiceLogCharge(diceLog, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.diceLogToDiceLogResponse(createdLog)), HttpStatus.CREATED);
    }

    @Operation(summary = "다이스 사용 로그", description = "다이스 사용 로그를 등록합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "다이스 사용 성공",
                            content = @Content(schema = @Schema(implementation = DiceLogDto.Response.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}")))
            }
    )
    @PostMapping("/used")
    public ResponseEntity<SingleResponseDto<DiceLogDto.Response>> postUsedLog(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "사용 로그 생성 요청 본문",
                                                                                      required = true, content = @Content(schema = @Schema(implementation = DiceLogDto.Post.class)))
                                                                              @RequestBody DiceLogDto.Post dto,
                                                                              @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        dto.setMemberId(customPrincipal.getMemberId());
        DiceLog diceLog = mapper.diceLogPostToDiceLog(dto);
        DiceLog createdLog = diceLogService.createDiceLogUsed(diceLog, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.diceLogToDiceLogResponse(createdLog)), HttpStatus.CREATED);
    }

    @Operation(summary = "다이스 로그 조회", description = "사용자가 다이스 로그 내역 목록을 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = DiceLogDto.Response.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping("/{member-id}")
    public ResponseEntity<MultiResponseDto<DiceLogDto.Response>> getMemberDiceLog(@Parameter(name = "member-id", description = "조회할 회원의 ID", example = "42")
                                                                                  @PathVariable("member-id") long memberId,
                                                                                  @Parameter(name = "page", description = "조회할 페이지 번호 (1부터 시작)", example = "1")
                                                                                  @RequestParam int page,
                                                                                  @Parameter(name = "size", description = "한 페이지당 로그 수", example = "10")
                                                                                  @RequestParam int size,
                                                                                  @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        AuthorizationUtils.isAdminOrOwner(memberId, customPrincipal.getMemberId());

        Page<DiceLog> logPage = diceLogService.findDiceLogs(page, size, memberId);
        List<DiceLog> logs = logPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.diceLogsToDiceLogResponses(logs), logPage), HttpStatus.OK);
    }


    // 관리자용 전체조회
    @Operation(summary = "전체 다이스 로그 조회", description = "관리자가 모든 회원의 다이스 로그 목록을 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = DiceLogDto.Response.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping("/logs")
    public ResponseEntity<MultiResponseDto<DiceLogDto.Response>> getAllDiceLogs(@Parameter(name = "page", description = "조회할 페이지 번호 (1부터 시작)", example = "1")
                                                                                @RequestParam int page,
                                                                                @Parameter(name = "size", description = "한 페이지당 로그 수", example = "10")
                                                                                @RequestParam int size,
                                                                                @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        AuthorizationUtils.isAdmin();
        Page<DiceLog> logPage = diceLogService.findAllDiceLogs(page, size);
        List<DiceLog> logs = logPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.diceLogsToDiceLogResponses(logs), logPage), HttpStatus.OK);
    }


}