package com.example.dice_talk.notice.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NoticeCreatedEvent {
    private final Long noticeId;
    private final String title;
}
