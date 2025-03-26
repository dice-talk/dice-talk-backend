package com.example.dice_talk.answer.service;

import com.example.dice_talk.answer.entity.Answer;
import com.example.dice_talk.answer.repository.AnswerRepository;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.question.entity.Question;
import com.example.dice_talk.question.service.QuestionService;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final MemberService memberService;
    private final QuestionService questionService;

    public AnswerService(AnswerRepository answerRepository, MemberService memberService, QuestionService questionService) {
        this.answerRepository = answerRepository;
        this.memberService = memberService;
        this.questionService = questionService;
    }

    public Answer createAnswer(Answer answer){
        memberService.findVerifiedMember(answer.getMember().getMemberId());
        AuthorizationUtils.isAdmin();
        Question question = verifyExistsAnswerInQuestion(answer);
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_ANSWERED);
        return answerRepository.save(answer);
    }

    public Answer updateAnswer(Answer answer){
        AuthorizationUtils.isAdmin();
        Answer findAnswer = findVerifiedAnswer(answer.getAnswerId());
        Optional.ofNullable(answer.getContent())
                .ifPresent(content -> findAnswer.setContent(answer.getContent()));
        return answerRepository.save(findAnswer);
    }

    @Transactional
    public void deleteAnswer(long answerId){
        AuthorizationUtils.isAdmin();
        Answer answer = findVerifiedAnswer(answerId);
        questionService.setAnswerNull(answer.getQuestion().getQuestionId());
        answerRepository.deleteById(answerId);
    }

    // 질문에 답변이 있는지 검증 후 질문 객체 반환
    private Question verifyExistsAnswerInQuestion(Answer answer) {
        // answer에 담긴 questionId로 question 있는지 검증 후 있으면 객체에 답변 있는지 검증
        Question question = questionService.findVerifiedQuestion(answer.getQuestion().getQuestionId());
        if (question.getAnswer() != null) {
            throw new BusinessLogicException(ExceptionCode.ANSWER_EXISTS);
        }
        return question;
    }

    // 답변이 존재하는지 검증
    private Answer findVerifiedAnswer(long answerId){
        Optional<Answer> optionalAnswer = answerRepository.findById(answerId);
        return optionalAnswer.orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.ANSWER_NOT_FOUND)
        );
    }
}
