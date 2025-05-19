package com.example.dice_talk.question.repository;

import com.example.dice_talk.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.questionStatus IN :statuses")
    Page<Question> findByQuestionStatusIn(@Param("statuses") List<Question.QuestionStatus> statuses, Pageable pageable);

//    @Query("SELECT q FROM Question q WHERE q.questionId = :questionId AND "
//            + "(q.member.memberId = :memberId OR :isAdmin = true)))")
//    Optional<Question> findQuestionByIdAndAccess(@Param("questionId") Long questionId,
//                                                 @Param("memberId") Long memberId,
//                                                 @Param("isAdmin") boolean isAdmin);

    @Query("SELECT q FROM Question q WHERE q.member.memberId = :memberId " +
            "AND q.questionStatus NOT IN ('QUESTION_DELETED', 'QUESTION_DEACTIVATED')")
    Page<Question> findAllActiveByMember_MemberId(@Param("memberId") Long memberId, Pageable pageable);
//    Page<Question> findAllByMember_MemberId(Long memberId, Pageable pageable);
}
