package com.example.dice_talk.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Schema(name = "DashboardChatRoomDto", description = "대시보드(채팅방) DTO")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardChatRoom {
    @Schema(description = "활성화 중인 전체 채팅방 수", example = "40")
    private int activeChatRoom;

    @Schema(description = "활성화 중인 단체 채팅방 수", example = "33")
    private int activeGroupChatRoom;

    @Schema(description = "활성화 중인 1대1 채팅방 수", example = "7")
    private int activeCoupleChatRoom;
}
