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
import com.example.dice_talk.question.mapper.QuestionMapper;
import com.example.dice_talk.question.service.QuestionService;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Parameter;
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

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.net.URI;
import java.util.List;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity postQuestion(@RequestParam("questionPostDto") String questionPostDtoString,
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

    @PatchMapping(value = "/{question-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity patchQuestion(
            @PathVariable("question-id") @Positive long questionId,
            @Valid @RequestParam("questionPatchDto") String questionPatchDtoString,
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

    @GetMapping("/{question-id}")
    public ResponseEntity getQuestion(
            @PathVariable("question-id") @Positive long questionId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Question question = questionService.findQuestion(
                questionId);
        return new ResponseEntity<>(new SingleResponseDto<>(questionMapper.questionToQuestionResponse(question)), HttpStatus.OK);
    }

    // 관리자용 전체조회
    @GetMapping("/office")
    public ResponseEntity getQuestions(@Positive @RequestParam int page, @Positive @RequestParam int size,
                                       @RequestParam(defaultValue = "newest") String sortType,
                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Member currentMember = memberService.findVerifiedMember(customPrincipal.getMemberId());
        Page<Question> questionPage = questionService.findQuestions(page, size, sortType, currentMember);
        List<Question> questions = questionPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>
                (questionMapper.questionsToQuestionResponses(questions), questionPage), HttpStatus.OK);
    }

    // 회원용 질문 목록 조회
    @GetMapping("/my-questions/{member-id}")
    public ResponseEntity getMyQuestions(@PathVariable("member-id") Long memberId,
                                         @Positive @RequestParam int page, @Positive @RequestParam int size,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        if(customPrincipal.getMemberId() != memberId){
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
        Page<Question> questionPage = questionService.findMyQuestions(page, size, memberId);
        List<Question> questions = questionPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>
                (questionMapper.questionsToQuestionResponses(questions), questionPage), HttpStatus.OK);
    }

    @DeleteMapping("/{question-id}")
    public ResponseEntity deleteQuestion(@PathVariable("question-id") long questionId,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        questionService.deleteQuestion(questionId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
