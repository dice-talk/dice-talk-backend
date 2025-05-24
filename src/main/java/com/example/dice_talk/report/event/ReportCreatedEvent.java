package com.example.dice_talk.report.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReportCreatedEvent {
    private final String reason;
    private final Long reporterId;
}
