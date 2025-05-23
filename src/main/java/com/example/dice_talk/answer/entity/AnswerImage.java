package com.example.dice_talk.answer.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerImage{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer answer;

    @Column(nullable = false)
    private String imageUrl;

    public void setAnswer(Answer answer){
        this.answer = answer;
        if(!answer.getImages().contains(this)){
            answer.setImage(this);
        }
    }
}
