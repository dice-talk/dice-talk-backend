package com.example.dice_talk.dicelog.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.dicelog.dto.DiceLogDto;
import com.example.dice_talk.dicelog.entity.DiceLog;
import com.example.dice_talk.dicelog.mapper.DiceLogMapper;
import com.example.dice_talk.dicelog.service.DiceLogService;
import com.example.dice_talk.utils.AuthorizationUtils;
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

    @PostMapping("/charge")
    public ResponseEntity postChargeLog(@RequestBody DiceLogDto.Post dto,
                                        @AuthenticationPrincipal CustomPrincipal customPrincipal){
        dto.setMemberId(customPrincipal.getMemberId());
        DiceLog diceLog = mapper.diceLogPostToDiceLog(dto);
        DiceLog createdLog = diceLogService.createDiceLogCharge(diceLog, customPrincipal.getMemberId());
        return new ResponseEntity(new SingleResponseDto<>(mapper.diceLogToDiceLogResponse(createdLog)), HttpStatus.CREATED);
    }

    @PostMapping("/used")
    public ResponseEntity postUsedLog(@RequestBody DiceLogDto.Post dto,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){
        dto.setMemberId(customPrincipal.getMemberId());
        DiceLog diceLog = mapper.diceLogPostToDiceLog(dto);
        DiceLog createdLog = diceLogService.createDiceLogUsed(diceLog, customPrincipal.getMemberId());
        return new ResponseEntity(new SingleResponseDto<>(mapper.diceLogToDiceLogResponse(createdLog)), HttpStatus.CREATED);
    }

    @GetMapping("/{member-id}")
    public ResponseEntity getMemberDiceLog(@PathVariable("member-id") long memberId,
                                           @RequestParam int page, @RequestParam int size,
                                           @AuthenticationPrincipal CustomPrincipal customPrincipal){

        AuthorizationUtils.isOwner(memberId, customPrincipal.getMemberId());

        Page<DiceLog> logPage = diceLogService.findDiceLogs(page, size, memberId);
        List<DiceLog> logs = logPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(mapper.diceLogsToDiceLogResponses(logs), logPage), HttpStatus.OK);
    }


    // 관리자용 전체조회
    @GetMapping("/logs")
    public ResponseEntity getAllDiceLogs(@RequestParam int page, @RequestParam int size,
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal){
        Page<DiceLog> logPage = diceLogService.findAllDiceLogs(page, size);
        List<DiceLog> logs = logPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(mapper.diceLogsToDiceLogResponses(logs), logPage), HttpStatus.OK);
    }


}
