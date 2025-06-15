//package com.example.dice_talk.question.service;
//
//import com.example.dice_talk.aws.S3Uploader;
//import com.example.dice_talk.exception.BusinessLogicException;
//import com.example.dice_talk.member.entity.Member;
//import com.example.dice_talk.member.service.MemberService;
//import com.example.dice_talk.question.dto.QuestionDto;
//import com.example.dice_talk.question.entity.Question;
//import com.example.dice_talk.question.enums.QuestionSearchType;
//import com.example.dice_talk.question.repository.QuestionImageRepository;
//import com.example.dice_talk.question.repository.QuestionRepository;
//import com.example.dice_talk.utils.AuthorizationUtils;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.security.authentication.TestingAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.mockStatic;
//import static org.assertj.core.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class QuestionServiceTest {
//
//    @InjectMocks
//    private QuestionService questionService;
//
//    @Mock
//    private QuestionRepository questionRepository;
//
//    @Mock
//    private MemberService memberService;
//
//    @Mock
//    private S3Uploader s3Uploader;
//
//    @Mock
//    private QuestionImageRepository questionImageRepository;
//
//    private final Member member = Member.builder()
//            .memberId(1L)
//            .email("user@test.com")
//            .questions(new ArrayList<>())
//            .build();
//
//    private final Question question = Question.builder()
//            .questionId(1L)
//            .title("제목")
//            .content("내용")
//            .questionStatus(Question.QuestionStatus.QUESTION_REGISTERED)
//            .member(member)
//            .build();
//
//    @Test
//    void createQuestion_success() throws IOException {
//        given(memberService.findVerifiedMember(anyLong())).willReturn(member);
//        given(questionRepository.save(any())).willReturn(question);
//        Question saved = questionService.createQuestion(question, null);
//        assertThat(saved).isEqualTo(question);
//    }
//
//    @Test
//    void createGuestQuestion_success() throws IOException {
//        QuestionDto.GuestPost guestPost = new QuestionDto.GuestPost("제목", "내용", "user@test.com");
//        given(memberService.findMemberByEmail(any())).willReturn(member);
//        given(questionRepository.save(any())).willReturn(question);
//        Question saved = questionService.createGuestQuestion(guestPost, null);
//        assertThat(saved).isEqualTo(question);
//    }
//
//    @Test
//    void updateQuestion_success() throws IOException {
//        Question updateQuestion = Question.builder()
//                .questionId(1L)
//                .title("수정된 제목")
//                .content("수정된 내용")
//                .member(member)
//                .build();
//        Question findQuestion = Question.builder()
//                .questionId(1L)
//                .title("원래 제목")
//                .content("원래 내용")
//                .member(member)
//                .build();
//        given(questionRepository.findById(anyLong())).willReturn(Optional.of(findQuestion));
//        given(questionRepository.save(any())).willReturn(findQuestion);
//        // SecurityContext mock
//        SecurityContextHolder.getContext().setAuthentication(
//                new TestingAuthenticationToken("user", "password", "ROLE_USER")
//        );
//        try (MockedStatic<AuthorizationUtils> utilities = mockStatic(AuthorizationUtils.class)) {
//            utilities.when(() -> AuthorizationUtils.isAdminOrOwner(anyLong(), anyLong())).thenAnswer(invocation -> null);
//            Question updated = questionService.updateQuestion(updateQuestion, null, List.of());
//            assertThat(updated).isEqualTo(findQuestion);
//        }
//    }
//
//    @Test
//    void updateQuestion_already_answered() {
//        Question answered = Question.builder()
//                .questionId(1L)
//                .questionStatus(Question.QuestionStatus.QUESTION_ANSWERED)
//                .member(member)
//                .build();
//        given(questionRepository.findById(anyLong())).willReturn(Optional.of(answered));
//        assertThatThrownBy(() -> questionService.updateQuestion(answered, null, List.of()))
//                .isInstanceOf(BusinessLogicException.class);
//    }
//
//    @Test
//    void deleteQuestion_success() {
//        given(questionRepository.findById(anyLong())).willReturn(Optional.of(question));
//        given(questionRepository.save(any())).willReturn(question);
//        // SecurityContext mock
//        SecurityContextHolder.getContext().setAuthentication(
//                new TestingAuthenticationToken("user", "password", "ROLE_USER")
//        );
//        try (MockedStatic<AuthorizationUtils> utilities = mockStatic(AuthorizationUtils.class)) {
//            utilities.when(() -> AuthorizationUtils.isOwner(anyLong(), anyLong())).thenReturn(true);
//            questionService.deleteQuestion(1L, 1L);
//            assertThat(question.getQuestionStatus()).isEqualTo(Question.QuestionStatus.QUESTION_DELETED);
//        }
//    }
//
//    @Test
//    void deleteQuestion_already_deleted() {
//        question.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);
//        assertThatThrownBy(() -> questionService.deleteQuestion(1L, 1L))
//                .isInstanceOf(BusinessLogicException.class);
//    }
//
//    @Test
//    void findQuestion_success() {
//        given(questionRepository.findById(anyLong())).willReturn(Optional.of(question));
//        Question result = questionService.findQuestion(1L);
//        assertThat(result).isEqualTo(question);
//    }
//
//    @Test
//    void findVerifiedQuestion_failed() {
//        given(questionRepository.findById(anyLong())).willReturn(Optional.empty());
//        assertThatThrownBy(() -> questionService.findVerifiedQuestion(1L))
//                .isInstanceOf(BusinessLogicException.class);
//    }
//
//    @Test
//    void isAnswered_failed() {
//        question.setQuestionStatus(Question.QuestionStatus.QUESTION_ANSWERED);
//        assertThatThrownBy(() -> questionService.isAnswered(1L))
//                .isInstanceOf(BusinessLogicException.class);
//    }
//
//    @Test
//    void findQuestions_success() {
//        Page<Question> page = new PageImpl<>(List.of(question));
//        given(questionRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);
//        Page<Question> result = questionService.findQuestions(1, 10, null, "desc", QuestionSearchType.TITLE, null);
//        assertThat(result.getContent()).hasSize(1);
//    }
//
//    @Test
//    void findMyQuestions_success() {
//        Page<Question> page = new PageImpl<>(List.of(question));
//        given(memberService.findVerifiedMember(anyLong())).willReturn(member);
//        given(questionRepository.findAllActiveByMember_MemberId(anyLong(), any())).willReturn(page);
//        Page<Question> result = questionService.findMyQuestions(1, 10, "desc", 1L);
//        assertThat(result.getContent()).hasSize(1);
//    }
//
//    @Test
//    void setAnswerNull_success() {
//        given(questionRepository.findById(anyLong())).willReturn(Optional.of(question));
//        questionService.setAnswerNull(1L);
//        assertThat(question.getAnswer()).isNull();
//    }
//
//    @Test
//    void findUnansweredQuestions_success() {
//        given(questionRepository.findAllByQuestionStatusOrderByCreatedAtDesc(any())).willReturn(List.of(question));
//        given(questionRepository.findAllByCreatedAtAfterAndQuestionStatusIn(any(), any())).willReturn(List.of(question));
//        List<?> result = questionService.findUnansweredQuestions();
//        assertThat(result).hasSize(1);
//    }
//}