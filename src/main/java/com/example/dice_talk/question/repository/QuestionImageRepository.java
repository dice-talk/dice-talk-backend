package com.example.dice_talk.question.repository;

import com.example.dice_talk.question.entity.QuestionImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionImageRepository extends JpaRepository<QuestionImage, Long>{
    List<QuestionImage> findAllByQuestion_QuestionId(Long questionId);
    void deleteByQuestionImageIdIn(List<Long> ids);
}
