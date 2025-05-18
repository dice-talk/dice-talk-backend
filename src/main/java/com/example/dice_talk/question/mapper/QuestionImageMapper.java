package com.example.dice_talk.question.mapper;

import com.example.dice_talk.question.dto.QuestionImageDto;
import com.example.dice_talk.question.entity.QuestionImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionImageMapper {
    @Mapping(target = "questionId", source = "question.questionId")
    QuestionImageDto.Response questionImageToResponse(QuestionImage questionImage);
    List<QuestionImageDto.Response> questionImagesToResponses(List<QuestionImage> questionImages);
}
