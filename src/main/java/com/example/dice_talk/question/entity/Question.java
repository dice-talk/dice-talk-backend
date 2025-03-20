package com.example.dice_talk.question.entity;

import com.example.dice_talk.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(value = EnumType.STRING)
    private QuestionStatus questionStatus = QuestionStatus.QUESTION_REGISTERED;

    @Column
    private String questionImage;

    @ManyToOne
    @JoinColumn(name = "member-id")
    private Member member;

    public enum QuestionStatus{
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

}
