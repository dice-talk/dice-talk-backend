package com.example.dice_talk.answer.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(nullable = false, length = 255)
    private String content;

    @Column
    private String answerImage;

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void setQuestion(Question question){
        this.question = question;
        if(question != null && question.getAnswer() != this){
            question.setAnswer(this);
        }
    }
}
