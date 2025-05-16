package com.example.dice_talk.answer.service;

import com.example.dice_talk.answer.entity.Answer;
import com.example.dice_talk.answer.entity.AnswerImage;
import com.example.dice_talk.answer.repository.AnswerRepository;
import com.example.dice_talk.aws.S3Uploader;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.question.entity.Question;
import com.example.dice_talk.question.service.QuestionService;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final MemberService memberService;
    private final QuestionService questionService;
    private final S3Uploader s3Uploader;

    public AnswerService(AnswerRepository answerRepository, MemberService memberService, QuestionService questionService, S3Uploader s3Uploader) {
        this.answerRepository = answerRepository;
        this.memberService = memberService;
        this.questionService = questionService;
        this.s3Uploader = s3Uploader;
    }

    public Answer createAnswer(Answer answer, List<MultipartFile> imageFiles) throws IOException {
        memberService.findVerifiedMember(answer.getMember().getMemberId());
        Question question = verifyExistsAnswerInQuestion(answer);
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_ANSWERED);
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = s3Uploader.upload(file, "answer-image");
                AnswerImage image = new AnswerImage();
                image.setImageUrl(imageUrl);
                image.setAnswer(answer);
                answer.getImages().add(image);
            }
        }
        return answerRepository.save(answer);
    }

    public Answer updateAnswer(Answer answer, List<MultipartFile> imageFiles, List<Long> keepImageIds) throws IOException {
        AuthorizationUtils.isAdmin();

        Answer findAnswer = findVerifiedAnswer(answer.getAnswerId());

        Optional.ofNullable(answer.getContent())
                .ifPresent(content -> findAnswer.setContent(answer.getContent()));

        List<AnswerImage> existingImages = findAnswer.getImages();
        List<AnswerImage> toRemove = existingImages.stream()
                .filter(image -> !keepImageIds.contains(image.getAnswerImageId()))
                .collect(Collectors.toList());

        // 기존 이미지 삭제 처리
        if (!findAnswer.getImages().isEmpty()) {
            for (AnswerImage image : toRemove) {
                s3Uploader.moveToDeletedFolder(image.getImageUrl(), "deleted-answer-image");
            }
            existingImages.removeAll(toRemove);
        }

        // 새 이미지 업로드 및 등록
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String imageUrl = s3Uploader.upload(file, "answer-image");

                AnswerImage image = new AnswerImage();
                image.setImageUrl(imageUrl);
                image.setAnswer(findAnswer);

                existingImages.add(image);
            }
        }

        return answerRepository.save(findAnswer);
    }

    @Transactional
    public void deleteAnswer(long answerId) {
        AuthorizationUtils.isAdmin();
        Answer answer = findVerifiedAnswer(answerId);
        questionService.setAnswerNull(answer.getQuestion().getQuestionId());
        if (!answer.getImages().isEmpty()) {
            for (AnswerImage answerImage : answer.getImages()) {
                s3Uploader.moveToDeletedFolder(answerImage.getImageUrl(), "deleted-answer-image");
            }
        }
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
    private Answer findVerifiedAnswer(long answerId) {
        return answerRepository.findById(answerId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.ANSWER_NOT_FOUND)
        );
    }
}
