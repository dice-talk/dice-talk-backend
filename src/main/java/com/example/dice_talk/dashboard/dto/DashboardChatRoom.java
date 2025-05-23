package com.example.dice_talk.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardChatRoom {
    private int activeChatRoom;

    private int activeGroupChatRoom;
    private int activeCoupleChatRoom;
}
