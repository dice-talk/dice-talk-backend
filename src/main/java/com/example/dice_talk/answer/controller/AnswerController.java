package com.example.dice_talk.answer.controller;

import com.example.dice_talk.answer.dto.AnswerDto;
import com.example.dice_talk.answer.entity.Answer;
import com.example.dice_talk.answer.mapper.AnswerMapper;
import com.example.dice_talk.answer.service.AnswerService;
import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.JsonParserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.List;

@Tag(name = "Answer API", description = "답변 관련 API 문서입니다.")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/questions/{question-id}/answers")
@Validated
public class AnswerController {
    private final AnswerService answerService;
    private final AnswerMapper mapper;
    private final JsonParserUtil jsonParserUtil;

    public AnswerController(AnswerService answerService, AnswerMapper mapper, JsonParserUtil jsonParserUtil) {
        this.answerService = answerService;
        this.mapper = mapper;
        this.jsonParserUtil = jsonParserUtil;
    }

    @Operation(summary = "답변 등록", description = "관리자가 새로운 답변을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "답변 등록 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "등록 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postAnswer(@Parameter(description = "답변을 등록할 질문글의 ID", example = "22")
                                               @PathVariable("question-id") Long questionId,
                                           @Parameter(description = "답변 JSON 문자열", example = "{\"content\": \"답변입니다.\"}")
                                     @RequestParam String answerPostDtoSTring,
                                           @Parameter(description = "첨부 이미지 목록", example = "image1.jpg", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
                                     @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws IOException {
        AuthorizationUtils.isAdmin();

        // JSON 문자열 -> DTO 수동 파싱
        AnswerDto.Post postDto = jsonParserUtil.parse(answerPostDtoSTring, AnswerDto.Post.class);

        postDto.setQuestionId(questionId);
        postDto.setMemberId(customPrincipal.getMemberId());

        Answer answer = mapper.answerPostToAnswer(postDto);
        answerService.createAnswer(answer, imageFiles);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "답변 수정", description = "관리자가 기존에 등록된 답변 내용, 이미지를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "답변 수정 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "수정 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 답변 수정 요청",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"The requested resource could not be found.\"}")))}
    )
    @PatchMapping(value = "/{answer-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> patchAnswer(@Parameter(description = "수정할 답변의 ID", example = "101")
                                                @PathVariable("answer-id") @Positive long answerId,
                                            @Parameter(description = "수정할 답변 본문 및 정보가 담긴 JSON 문자열", example = "{\"content\": \"수정된 답변입니다.\", \"isPublic\": true}")
                                      @RequestParam("answerPatchDto") String answerPatchDtoString,
                                            @Parameter(description = "답변에 첨부할 이미지 파일 목록 (선택)", example = "image1.jpg, image2.png", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
                                      @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) throws IOException {
        AuthorizationUtils.isAdmin();
        // JSON -> DTO 변환
        AnswerDto.Patch patchDto = jsonParserUtil.parse(answerPatchDtoString, AnswerDto.Patch.class);

        patchDto.setAnswerId(answerId);
        Answer updatedAnswer = answerService.updateAnswer(mapper.answerPatchToAnswer(patchDto), imageFiles, patchDto.getKeepImageIds());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "답변 삭제", description = "특정 답변을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "답변 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "삭제 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @DeleteMapping("/{answer-id}")
    public ResponseEntity<Void> deleteAnswer(@Parameter(name = "answer-id", description = "삭제할 답변의 ID", example = "101")
                                                 @PathVariable("answer-id") long answerId) {
        AuthorizationUtils.isAdmin();
        answerService.deleteAnswer(answerId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
