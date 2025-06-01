package com.example.dice_talk.question.entity;

import com.example.dice_talk.answer.entity.Answer;
import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(value = EnumType.STRING)
    private QuestionStatus questionStatus = QuestionStatus.QUESTION_REGISTERED;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Answer answer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionImage> images = new ArrayList<>();

    public void deactivate(){
        this.questionStatus = QuestionStatus.QUESTION_DEACTIVED;
    }

    public void setMember(Member member){
        this.member = member;
        if(!member.getQuestions().contains(this)){
            member.setQuestion(this);
        }
    }

    public void setAnswer(Answer answer){
        this.answer = answer;
        if(answer != null){
            answer.setQuestion(this);
            this.questionStatus = QuestionStatus.QUESTION_ANSWERED;
        }
    }

    public enum QuestionStatus{
        QUESTION_GUEST("비회원 문의"),
        QUESTION_GUEST_ANSWERED("비회원 문의 답변 완료 상태"),
        QUESTION_REGISTERED("문의 등록 상태"),
        QUESTION_ANSWERED("답변 완료 상태"),
        QUESTION_DELETED("문의 삭제 상태"),
        QUESTION_DEACTIVED("문의 비활성화 상태");

        @Getter
        private String status;

        QuestionStatus(String status) {
            this.status = status;
        }
    }

    public void setImage(QuestionImage questionImage){
        if(questionImage.getQuestion() != this){
            questionImage.setQuestion(this);
        }
        if(!this.images.contains(questionImage)){
            images.add(questionImage);
        }
    }

}
