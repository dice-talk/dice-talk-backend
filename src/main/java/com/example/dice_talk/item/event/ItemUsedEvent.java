package com.example.dice_talk.item.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ItemUsedEvent {
    private final Long memberId;
    private final String itemName;
    private final int quantity;
}
