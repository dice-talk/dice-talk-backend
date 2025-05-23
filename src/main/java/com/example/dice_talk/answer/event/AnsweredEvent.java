package com.example.dice_talk.answer.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AnsweredEvent {
    private final Long memberId;
    private final Long questionId;
}
