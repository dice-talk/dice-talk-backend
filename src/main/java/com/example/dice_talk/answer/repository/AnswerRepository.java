package com.example.dice_talk.answer.repository;

import com.example.dice_talk.answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
