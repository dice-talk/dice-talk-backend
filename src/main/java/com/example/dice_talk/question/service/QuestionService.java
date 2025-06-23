package com.example.dice_talk.question.service;

import com.example.dice_talk.aws.S3Uploader;
import com.example.dice_talk.dashboard.dto.DashboardQuestion;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.question.dto.QuestionDto;
import com.example.dice_talk.question.entity.Question;
import com.example.dice_talk.question.entity.QuestionImage;
import com.example.dice_talk.question.enums.QuestionSearchType;
import com.example.dice_talk.question.repository.QuestionImageRepository;
import com.example.dice_talk.question.repository.QuestionRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final MemberService memberService;
    private final S3Uploader s3Uploader;
    private final QuestionImageRepository questionImageRepository;

    public QuestionService(QuestionRepository questionRepository, MemberService memberService, S3Uploader s3Uploader, QuestionImageRepository questionImageRepository) {
        this.questionRepository = questionRepository;
        this.memberService = memberService;
        this.s3Uploader = s3Uploader;
        this.questionImageRepository = questionImageRepository;
    }

    @Transactional
    public Question createQuestion(Question question, List<MultipartFile> imageFiles) throws IOException {
        memberService.findVerifiedMember(question.getMember().getMemberId());
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_REGISTERED);
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = s3Uploader.upload(file, "question-image");
                QuestionImage image = new QuestionImage();
                image.setImageUrl(imageUrl);
                image.setQuestion(question);
                question.setImage(image);
            }
        }
        return questionRepository.save(question);
    }

    @Transactional
    public Question createGuestQuestion(QuestionDto.GuestPost guestPost, List<MultipartFile> imageFiles) throws IOException {
        Member member = memberService.findMemberByEmail(guestPost.getEmail());
        Question question = new Question();
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_GUEST);
        question.setMember(member);
        question.setTitle(guestPost.getTitle());
        question.setContent(guestPost.getContent());
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = s3Uploader.upload(file, "question-image");
                QuestionImage image = new QuestionImage();
                image.setImageUrl(imageUrl);
                image.setQuestion(question);
                question.setImage(image);
            }
        }
        return questionRepository.save(question);
    }

    @Transactional
    public Question updateQuestion(Question question, List<MultipartFile> imageFiles, List<Long> keepImageIds) throws IOException {
        // 답변 완료시 수정 불가능
        isAnswered(question.getQuestionId());
        // 제목, 내용, visibility
        Question findQuestion = findVerifiedQuestion(question.getQuestionId());
        // 작성자인지 확인
        AuthorizationUtils.isAdminOrOwner(question.getMember().getMemberId(), findQuestion.getMember().getMemberId());
        Optional.ofNullable(question.getTitle())
                .ifPresent(title -> findQuestion.setTitle(title));
        Optional.ofNullable(question.getContent())
                .ifPresent(content -> findQuestion.setContent(content));

        List<QuestionImage> existingImages = findQuestion.getImages();
        List<QuestionImage> toRemove = existingImages.stream()
                .filter(image -> !keepImageIds.contains(image.getQuestionImageId()))
                .collect(Collectors.toList());

        // 기존 이미지 S3 삭제(폴더 이동) + DB 삭제 + 영속성 제거
        for (QuestionImage image : toRemove) {
            s3Uploader.moveToDeletedFolder(image.getImageUrl(), "deleted-question-image");
        }
        questionImageRepository.deleteAll(toRemove);
        existingImages.removeAll(toRemove);


        // 새 이미지 업로드 및 등록
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = s3Uploader.upload(file, "question-image");

                QuestionImage image = new QuestionImage();
                image.setImageUrl(imageUrl);
                image.setQuestion(findQuestion);

//                existingImages.add(image);
            }
        }
        return questionRepository.save(findQuestion);
    }

    public Page<Question> findQuestions(int page, int size, Question.QuestionStatus status, String sort, QuestionSearchType searchType, String keyword) {
        if (page < 1) throw new IllegalArgumentException("페이지는 1 이상이어야 합니다.");
        if (size < 1) throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");

        // Pageable 생성(createdAt 기준 최신순, 오래된 순)
        Sort s = "asc".equalsIgnoreCase(sort)
                ? Sort.by("questionId").ascending()
                : Sort.by("questionId").descending();
        Pageable pageable = PageRequest.of(page - 1, size, s);

        // Specification 빌드
        Specification<Question> spec = Specification.where(null);

        // QuestionStatus 로 필터링
        if (status != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("questionStatus"), status));
        }

        // 키워드 검색 (title, content, member.name)
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim() + "%";
            switch (searchType) {
                case TITLE:
                    spec = spec.and((root, query, cb) ->
                            cb.like(root.get("title"), pattern)
                    );
                    break;
                case AUTHOR:
                    spec = spec.and((root, query, cb) -> {
                        Join<Question, Member> m = root.join("member", JoinType.LEFT);
                        return cb.like(m.get("email"), pattern);
                    });
                    break;
                case TITLE_AUTHOR:
                    spec = spec.and((root, query, cb) -> {
                        Join<Question, Member> m = root.join("member", JoinType.LEFT);
                        return cb.or(
                                cb.like(root.get("title"), pattern),
                                cb.like(m.get("email"), pattern)
                        );
                    });
                    break;
                case CONTENT:
                    spec = spec.and((root, query, cb) ->
                            cb.like(root.get("content"), pattern)
                    );
                    break;
            }
            ;
        }

        return questionRepository.findAll(spec, pageable);
    }

    public Page<Question> findMyQuestions(int page, int size, String sort, Long memberId) {
        if (page < 1) throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        Pageable pageable;
        if(sort.toUpperCase().equals("ASC")){
             pageable = PageRequest.of(page - 1, size, Sort.by("questionId").ascending());
        } else {
             pageable = PageRequest.of(page - 1, size, Sort.by("questionId").descending());
        }
        memberService.findVerifiedMember(memberId);
        return questionRepository.findAllActiveByMember_MemberId(memberId, pageable);
    }

    // 비회원 질문 목록 조회
    public Page<Question> findGuestQuestions(
            int page, int size, Question.QuestionStatus status, String search, String searchType, String sort
    ){
        if (page < 1) throw new IllegalArgumentException("페이지는 1 이상이어야 합니다.");
        if (size < 1) throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");

        Sort st = "asc".equalsIgnoreCase(sort) ?
                Sort.by("questionId").ascending() :
                Sort.by("questionId").descending();
        Pageable pageable = PageRequest.of(page - 1, size, st);

        return questionRepository.searchGuestQuestions(status, search, searchType, pageable);
    }

    public Question findQuestion(Long questionId) {
        // Authentication 통해서 memberId와 관리자인지 받아와서 권한 없는 글에 접근 시 예외처리
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));
    }

    @Transactional
    public void deleteQuestion(Long questionId, long memberId) {
        // 질문 존재 확인해서 가져오고
        Question findQuestion = findVerifiedQuestion(questionId);
        // 작성자와 현재 사용자 같은지 확인
        AuthorizationUtils.isOwner(findQuestion.getMember().getMemberId(), memberId);
        // 이미 삭제 상태인지 확인
        verifyQuestionStatus(findQuestion);
        if (!findQuestion.getImages().isEmpty()) {
            for (QuestionImage image : findQuestion.getImages()) {
                s3Uploader.moveToDeletedFolder(image.getImageUrl(), "deleted-question-image");
            }
        }
        // 상태 변경
        findQuestion.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);

        // 저장
        questionRepository.save(findQuestion);

    }

    // 질문 존재하는지 검증
    public Question findVerifiedQuestion(Long questionId) {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        return optionalQuestion.orElseThrow(() -> new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND));
    }

    // 답변은 하나밖에 못하기 때문에 있는지 검증
    public void isAnswered(Long questionId) {
        if (findVerifiedQuestion(questionId).getQuestionStatus() == Question.QuestionStatus.QUESTION_ANSWERED) {
            throw new BusinessLogicException(ExceptionCode.CANNOT_CHANGE_QUESTION);
        }
    }

    // 질문이 삭제 상태인지 검증
    public void verifyQuestionStatus(Question question) {
        if (question.getQuestionStatus() == Question.QuestionStatus.QUESTION_DELETED) {
            throw new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND);
        }
    }

    // 정렬조건 설정
    private Sort getSortType(String sortType) {
        switch (sortType.toUpperCase()) {
            case "NEWEST":
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case "OLDEST":
                return Sort.by(Sort.Direction.ASC, "createdAt");
            default:
                throw new IllegalArgumentException("올바른 정렬 조건을 입력해 주세요: " + sortType);
        }
    }

    // 답변 삭제 시 질문의 answer null로 만드는 메서드
    public void setAnswerNull(long questionId) {
        Question question = findVerifiedQuestion(questionId);
        question.setAnswer(null);
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_REGISTERED);
    }

    public List<DashboardQuestion> findUnansweredQuestions() {
        //미답변 질문글만 조회
        List<Question> questions = questionRepository.findAllByQuestionStatusOrderByCreatedAtDesc(Question.QuestionStatus.QUESTION_REGISTERED);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Question.QuestionStatus> statuses = List.of(
                Question.QuestionStatus.QUESTION_REGISTERED,
                Question.QuestionStatus.QUESTION_ANSWERED
        );
        //7일간 작성된 질문 글 조회
        List<Question> weeklyQuestions = questionRepository.findAllByCreatedAtAfterAndQuestionStatusIn(sevenDaysAgo, statuses);

        return questions.stream().map(question -> new DashboardQuestion(question.getTitle(), questions.size(), weeklyQuestions.size()))
                .collect(Collectors.toList());
    }


}
