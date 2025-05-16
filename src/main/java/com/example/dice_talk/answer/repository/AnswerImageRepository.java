package com.example.dice_talk.answer.repository;

import com.example.dice_talk.answer.entity.AnswerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerImageRepository extends JpaRepository<AnswerImage, Long> {
    List<AnswerImage> findAllByAnswer_AnswerId(Long answerId);
}
