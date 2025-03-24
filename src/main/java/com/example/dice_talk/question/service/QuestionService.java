package com.example.dice_talk.question.service;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.question.entity.Question;
import com.example.dice_talk.question.repository.QuestionRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final MemberService memberService;

    public QuestionService(QuestionRepository questionRepository, MemberService memberService) {
        this.questionRepository = questionRepository;
        this.memberService = memberService;
    }

    public Question createQuestion(Question question, Long memberId){
        memberService.findVerifiedMember(memberId);
        return questionRepository.save(question);
    }

    public Question updateQuestion(Question question, Long memberId){
        // 작성자인지 확인
        AuthorizationUtils.isAdminOrOwner(question.getMember().getMemberId(), memberId);
        // 답변 완료시 수정 불가능
        isAnswered(question.getQuestionId());
        // 제목, 내용, visibility
        Question findQuestion = findVerifiedQuestion(question.getQuestionId());
        Optional.ofNullable(question.getTitle())
                .ifPresent(title -> findQuestion.setTitle(title));
        Optional.ofNullable(question.getContent())
                .ifPresent(content -> findQuestion.setContent(content));
        return questionRepository.save(findQuestion);
    }

    public Page<Question> findQuestions(int page, int size, String sortType, Member currentMember){
        // 페이지 번호 검증
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        // 정렬 조건 설정
        if(sortType == null || sortType.isBlank()){
            sortType = "newest";
        }
        Sort sort = getSortType(sortType);
        Pageable pageable = PageRequest.of(page -1, size, sort);
        // 비활성화 글 제외하고 조회
        Page<Question> questionPage = questionRepository.findAllQuestionsWithoutDeactivated(pageable);
        return questionPage;
    }

    public Page<Question> findMyQuestions(int page, int size, Long memberId){
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        Pageable pageable = PageRequest.of(page -1, size, Sort.by("question_id").descending());
        memberService.findVerifiedMember(memberId);
        return questionRepository.findAllByMemberId(memberId, pageable);
    }

    public Question findQuestion(Long questionId){
        // Authentication 통해서 memberId와 관리자인지 받아와서 권한 없는 글에 접근 시 예외처리
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));
    }

    public void deleteQuestion(Long questionId, long memberId){
        // 질문 존재 확인해서 가져오고
        Question findQuestion = findVerifiedQuestion(questionId);
        // 작성자와 현재 사용자 같은지 확인
        AuthorizationUtils.isOwner(findQuestion.getMember().getMemberId(), memberId);
        // 이미 삭제 상태인지 확인
        verifyQuestionStatus(findQuestion);
        // 상태 변경
        findQuestion.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);
        // 저장
        questionRepository.save(findQuestion);

    }

    // 질문 존재하는지 검증
    public Question findVerifiedQuestion(Long questionId){
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        return optionalQuestion.orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));
    }

    // 답변은 하나밖에 못하기 때문에 있는지 검증
    public void isAnswered(Long questionId){
        if(findVerifiedQuestion(questionId).getQuestionStatus() == Question.QuestionStatus.QUESTION_ANSWERED){
            throw new BusinessLogicException(ExceptionCode.CANNOT_CHANGE_QUESTION);
        }
    }

    // 질문이 삭제 상태인지 검증
    public void verifyQuestionStatus(Question question){
        if(question.getQuestionStatus() == Question.QuestionStatus.QUESTION_DELETED){
            throw new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND);
        }
    }

    // 정렬조건 설정
    private Sort getSortType(String sortType){
        switch (sortType.toUpperCase()){
            case "NEWEST":
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case "OLDEST":
                return Sort.by(Sort.Direction.ASC, "createdAt");
            default:
                throw new IllegalArgumentException("올바른 정렬 조건을 입력해 주세요: " + sortType);
        }
    }

    // 답변 삭제 시 질문의 answer null로 만드는 메서드
    public void setAnswerNull(long questionId){
        findVerifiedQuestion(questionId).setAnswer(null);
    }
}
