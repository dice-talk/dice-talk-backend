package com.example.dice_talk.dicelog.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.dicelog.dto.DiceLogDto;
import com.example.dice_talk.dicelog.entity.DiceLog;
import com.example.dice_talk.dicelog.mapper.DiceLogMapper;
import com.example.dice_talk.dicelog.service.DiceLogService;
import com.example.dice_talk.response.ErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

    @Operation(
            summary = "다이스 로그 생성",
            description = "다이스 충전 로그를 등록합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/charge")
    public ResponseEntity postChargeLog(@RequestBody DiceLogDto.Post dto,
                                        @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        dto.setMemberId(customPrincipal.getMemberId());
        DiceLog diceLog = mapper.diceLogPostToDiceLog(dto);
        DiceLog createdLog = diceLogService.createDiceLogCharge(diceLog, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.diceLogToDiceLogResponse(createdLog)), HttpStatus.CREATED);
    }

    @Operation(
            summary = "다이스 로그 생성",
            description = "다이스 사용 로그를 등록합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/used")
    public ResponseEntity postUsedLog(@RequestBody DiceLogDto.Post dto,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        dto.setMemberId(customPrincipal.getMemberId());
        DiceLog diceLog = mapper.diceLogPostToDiceLog(dto);
        DiceLog createdLog = diceLogService.createDiceLogUsed(diceLog, customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.diceLogToDiceLogResponse(createdLog)), HttpStatus.CREATED);
    }

    @Operation(
            summary = "다이스 로그 조회",
            description = "회원의 다이스 로그 내역을 페이징 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/{member-id}")
    public ResponseEntity getMemberDiceLog(@PathVariable("member-id") long memberId,
                                           @RequestParam int page, @RequestParam int size,
                                           @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        AuthorizationUtils.isAdminOrOwner(memberId, customPrincipal.getMemberId());

        Page<DiceLog> logPage = diceLogService.findDiceLogs(page, size, memberId);
        List<DiceLog> logs = logPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.diceLogsToDiceLogResponses(logs), logPage), HttpStatus.OK);
    }


    // 관리자용 전체조회
    @Operation(
            summary = "전체 다이스 로그 조회",
            description = "관리자 전용 - 모든 회원의 다이스 로그를 페이징 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/logs")
    public ResponseEntity getAllDiceLogs(@RequestParam int page, @RequestParam int size,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        AuthorizationUtils.isAdmin();
        Page<DiceLog> logPage = diceLogService.findAllDiceLogs(page, size);
        List<DiceLog> logs = logPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.diceLogsToDiceLogResponses(logs), logPage), HttpStatus.OK);
    }


}
