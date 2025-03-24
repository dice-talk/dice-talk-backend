package com.example.dice_talk.question.mapper;

import com.example.dice_talk.answer.dto.AnswerDto;
import com.example.dice_talk.answer.entity.Answer;
import com.example.dice_talk.question.dto.QuestionDto;
import com.example.dice_talk.question.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    Question questionPostToQuestion(QuestionDto.Post postDto);
    @Mapping(target = "member.memberId", source = "memberId")
    Question questionPatchToQuestion(QuestionDto.Patch patchDto);
    AnswerDto.Response answerToAnswerResponse(Answer answer);
    @Mapping(target = "answer", source = "answer")
    @Mapping(target = "memberId", source = "member.memberId")
    QuestionDto.Response questionToQuestionResponse(Question question);
    List<QuestionDto.Response> questionsToQuestionResponses(List<Question> questions);
}
