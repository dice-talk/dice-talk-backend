package com.example.dice_talk.question.repository;

import com.example.dice_talk.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.questionStatus != 'QUESTION_DEACTIVATED'")
    Page<Question> findAllQuestionsWithoutDeactivated(Pageable pageable);

//    @Query("SELECT q FROM Question q WHERE q.questionId = :questionId AND "
//            + "(q.member.memberId = :memberId OR :isAdmin = true)))")
//    Optional<Question> findQuestionByIdAndAccess(@Param("questionId") Long questionId,
//                                                 @Param("memberId") Long memberId,
//                                                 @Param("isAdmin") boolean isAdmin);

    @Query("SELECT q FROM Question q WHERE q.member.memberId = :memberId")
    Page<Question> findAllByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
