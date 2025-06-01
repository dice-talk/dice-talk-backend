package com.example.dice_talk.answer.mapper;

import com.example.dice_talk.answer.dto.AnswerDto;
import com.example.dice_talk.answer.dto.AnswerImageDto;
import com.example.dice_talk.answer.entity.Answer;
import com.example.dice_talk.answer.entity.AnswerImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(target = "member.memberId", source = "memberId")
    @Mapping(target = "question.questionId", source = "questionId")
    Answer answerPostToAnswer(AnswerDto.Post postDto);

    Answer answerPatchToAnswer(AnswerDto.Patch patchDto);

    @Mapping(target = "answerImages", source = "images")
    @Mapping(target = "questionId", source = "question.questionId")
    @Mapping(target = "memberId", source = "question.member.memberId")
    AnswerDto.Response answerToAnswerResponse(Answer answer);

    default List<AnswerImageDto.Response> answerImagesToResponses(List<AnswerImage> images){
        if(images == null) return List.of();
        return images.stream().map(image -> new AnswerImageDto.Response(
                image.getAnswerImageId(),
                image.getAnswer().getAnswerId(),
                image.getImageUrl()
        )).collect(Collectors.toList());
    }

    default List<AnswerImage> mapToAnswerImages(List<String> imageUrls, Answer answer){
        if (imageUrls == null) return List.of();
        return imageUrls.stream().map(url -> {
            AnswerImage image = new AnswerImage();
            image.setImageUrl(url);
            image.setAnswer(answer);
            return image;
        }).collect(Collectors.toList());
    }
}
