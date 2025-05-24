package com.example.dice_talk.report.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReportCompletedEvent {
    private final Long reportId;
    private final int warnCount;
    private final String reason;
    private final String date;
    private final Long reportedMemberId;
}
