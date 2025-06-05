package com.example.dice_talk.question.repository;

import com.example.dice_talk.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionRepositoryCustom {
    Page<Question> searchGuestQuestions(
            Question.QuestionStatus status,
            String search,
            String searchType,
            Pageable pageable
    );
}
