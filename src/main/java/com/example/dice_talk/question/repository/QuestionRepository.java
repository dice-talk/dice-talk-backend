package com.example.dice_talk.question.repository;

import com.example.dice_talk.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
