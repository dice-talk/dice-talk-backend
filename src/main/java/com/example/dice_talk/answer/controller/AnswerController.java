package com.example.dice_talk.answer.controller;

import com.example.dice_talk.answer.dto.AnswerDto;
import com.example.dice_talk.answer.entity.Answer;
import com.example.dice_talk.answer.mapper.AnswerMapper;
import com.example.dice_talk.answer.service.AnswerService;
import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.utils.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Tag(name = "Answer API", description = "답변 관련 API 문서입니다.")
@RestController
@RequestMapping("/questions/{question-id}/answers")
@Validated
public class AnswerController {
    private final AnswerService answerService;
    private final AnswerMapper mapper;

    public AnswerController(AnswerService answerService, AnswerMapper mapper) {
        this.answerService = answerService;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Answer Post API",
            description = "답변을 등록합니다.",
            // security = @SecurityRequirement(name = "JWT")
            responses = {
                    @ApiResponse(responseCode = "201", description = "답변 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PostMapping
    public ResponseEntity postAnswer(@PathVariable("question-id") Long questionId,
                                     @Valid @RequestBody AnswerDto.Post postDto,
                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        AuthorizationUtils.isAdmin();
        postDto.setQuestionId(questionId);
        postDto.setMemberId(customPrincipal.getMemberId());
        Answer answer = mapper.answerPostToAnswer(postDto);
        Answer createdAnswer = answerService.createAnswer(answer);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
            summary = "Answer Patch API",
            description = "답변을 수정합니다.",
            // security = @SecurityRequirement(name = "JWT")
            responses = {
                    @ApiResponse(responseCode = "200", description = "답변 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "해당 답변 없음")
            }
    )
    @PatchMapping("/{answer-id}")
    public ResponseEntity patchAnswer(@PathVariable("answer-id") @Positive long answerId,
                                      @Valid @RequestBody AnswerDto.Patch patchDto) {
        AuthorizationUtils.isAdmin();
        patchDto.setAnswerId(answerId);
        Answer updatedAnswer = answerService.updateAnswer(mapper.answerPatchToAnswer(patchDto));
        return new ResponseEntity(HttpStatus.OK);
    }

    @Operation(
            summary = "Answer Delete API",
            description = "답변을 삭제합니다.",
            // security = @SecurityRequirement(name = "JWT")
            responses = {
                    @ApiResponse(responseCode = "204", description = "답변 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "해당 답변 없음")
            }
    )
    @DeleteMapping("/{answer-id}")
    public ResponseEntity deleteAnswer(@PathVariable("answer-id") long answerId) {
        AuthorizationUtils.isAdmin();
        answerService.deleteAnswer(answerId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
