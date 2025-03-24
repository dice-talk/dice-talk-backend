package com.example.dice_talk.answer.mapper;

import com.example.dice_talk.answer.dto.AnswerDto;
import com.example.dice_talk.answer.entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "question.questionId", source = "questionId")
    Answer answerPostToAnswer(AnswerDto.Post postDto);

    Answer answerPatchToAnswer(AnswerDto.Patch patchDto);
}
