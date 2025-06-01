package com.example.dice_talk.question.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.question.dto.QuestionDto;
import com.example.dice_talk.question.entity.Question;
import com.example.dice_talk.question.enums.QuestionSearchType;
import com.example.dice_talk.question.mapper.QuestionMapper;
import com.example.dice_talk.question.service.QuestionService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Tag(name = "Question", description = "질문 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/questions")
@Validated
@Slf4j
public class QuestionController {
    private static final String QUESTION_DEFAULT_URL = "/questions";
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final MemberService memberService;
    private final JsonParserUtil jsonParserUtil;

    public QuestionController(QuestionService questionService, QuestionMapper questionMapper, MemberService memberService, JsonParserUtil jsonParserUtil) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.memberService = memberService;
        this.jsonParserUtil = jsonParserUtil;
    }

    @Operation(summary = "질문 등록", description = "새로운 질문을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postQuestion(@Parameter(description = "질문 생성 DTO 문자열(JSON)")
                                             @RequestParam("questionPostDto") String questionPostDtoString,
                                             @Parameter(description = "질문 이미지 파일 목록", required = false)
                                             @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                             @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws IOException {
        QuestionDto.Post questionPostDto = jsonParserUtil.parse(questionPostDtoString, QuestionDto.Post.class);
        // dto에 memberId set
        questionPostDto.setMemberId(customPrincipal.getMemberId());
        // mapper로 dto -> entity
        Question question = questionMapper.questionPostToQuestion(questionPostDto);
        // question 만들고
        Question createdQuestion = questionService.createQuestion(question, imageFiles);
        // URI 만들기
        URI location = UriCreator.createUri(QUESTION_DEFAULT_URL, createdQuestion.getQuestionId());
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "질문 수정", description = "기존 등록된 질문을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = QuestionDto.Response.class))
            ),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @PatchMapping(value = "/{question-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponseDto<QuestionDto.Response>> patchQuestion(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable("question-id") @Positive long questionId,
            @Parameter(description = "질문 수정 DTO 문자열(JSON)")
            @RequestParam("questionPatchDto") String questionPatchDtoString,
            @Parameter(description = "질문 이미지 파일 목록", required = false)
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws IOException {
        QuestionDto.Patch patchDto = jsonParserUtil.parse(questionPatchDtoString, QuestionDto.Patch.class);
        patchDto.setQuestionId(questionId);
        patchDto.setMemberId(customPrincipal.getMemberId());
        Question updatedQuestion = questionService.updateQuestion(questionMapper
                .questionPatchToQuestion(patchDto), imageFiles, patchDto.getKeepImageIds());
        return new ResponseEntity<>(
                new SingleResponseDto<>(questionMapper.questionToQuestionResponse(updatedQuestion)), HttpStatus.OK);
    }

    @Operation(summary = "질문 상세 조회", description = "특정 질문을 상세조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = QuestionDto.Response.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @GetMapping("/{question-id}")
    public ResponseEntity<SingleResponseDto<QuestionDto.Response>> getQuestion(
            @Parameter(description = "질문 ID", example = "1")
            @PathVariable("question-id") @Positive long questionId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Question question = questionService.findQuestion(
                questionId);
        AuthorizationUtils.isAdminOrOwner(question.getMember().getMemberId(), customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(questionMapper.questionToQuestionResponse(question)), HttpStatus.OK);
    }

    // 관리자용 전체조회
    @Operation(summary = "관리자 전체 질문 조회", description = "전체 질문 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = QuestionDto.Response.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 파라미터",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping("/admin")
    public ResponseEntity<MultiResponseDto<QuestionDto.Response>> getQuestions(@Parameter(description = "페이지 번호(1 이상)", example = "1")
                                                                               @Positive @RequestParam(defaultValue = "1") int page,
                                                                               @Parameter(description = "페이지 크기(1 이상)", example = "10")
                                                                               @Positive @RequestParam(defaultValue = "10") int size,
                                                                               @Parameter(description = "질문 상태", example = "QUESTION_REGISTERED")
                                                                               @RequestParam(required = false) Question.QuestionStatus status,
                                                                               @Parameter(description = "정렬 타입", example = "latest")
                                                                               @RequestParam(defaultValue = "latest") String sortOrder,
                                                                               @Parameter(description = "검색 범위", example = "TITLE_AUTHOR")
                                                                               @RequestParam(defaultValue = "TITLE_AUTHOR") QuestionSearchType searchType,
                                                                               @RequestParam(required = false) String keyword) {
        Page<Question> questionPage = questionService.findQuestions(page, size, status, sortOrder, searchType, keyword);
        List<QuestionDto.Response> responses =
                questionMapper.questionsToQuestionResponses(questionPage.getContent());
        return new ResponseEntity<>(new MultiResponseDto<>
                (responses, questionPage), HttpStatus.OK);
    }

    // 회원용 질문 목록 조회
    @Operation(summary = "회원 질문 조회", description = "회원이 작성한 질문 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = QuestionDto.Response.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 본인만 조회 가능",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping("/my-questions")
    public ResponseEntity<MultiResponseDto<QuestionDto.Response>> getMyQuestions(@Parameter(description = "페이지 번호(1 이상)", example = "1")
                                                                                 @Positive @RequestParam int page,
                                                                                 @Parameter(description = "페이지 크기(1 이상)", example = "10")
                                                                                 @Positive @RequestParam int size,
                                                                                 @Parameter(description = "정렬방식", example = "asc / desc")
                                                                                 @RequestParam(value = "sort") String sort,
                                                                                 @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Page<Question> questionPage = questionService.findMyQuestions(page, size, sort, customPrincipal.getMemberId());
        List<Question> questions = questionPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>
                (questionMapper.questionsToQuestionResponses(questions), questionPage), HttpStatus.OK);
    }

    @Operation(summary = "질문 삭제", description = "특정 질문을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @DeleteMapping("/{question-id}")
    public ResponseEntity<Void> deleteQuestion(@Parameter(description = "질문 ID", example = "1")
                                               @PathVariable("question-id") long questionId,
                                               @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        questionService.deleteQuestion(questionId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "비회원 질문 등록", description = "비회원이 새로운 질문을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            )
    })
    @PostMapping(value = "/guest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postQuestionForGuest(@Parameter(description = "질문 생성 DTO 문자열(JSON)")
                                             @RequestParam("guestQuestionPostDto") String guestQuestionPostDtoString,
                                             @Parameter(description = "질문 이미지 파일 목록", required = false)
                                             @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) throws IOException {
        QuestionDto.GuestPost guestQuestionPostDto = jsonParserUtil.parse(guestQuestionPostDtoString, QuestionDto.GuestPost.class);

        // question 만들고
        Question createdQuestion = questionService.createGuestQuestion(guestQuestionPostDto, imageFiles);
        // URI 만들기
        URI location = UriCreator.createUri(QUESTION_DEFAULT_URL, createdQuestion.getQuestionId());
        return ResponseEntity.created(location).build();
    }
}
