package com.example.dice_talk.Home.dto;

import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.theme.dto.ThemeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Schema(name = "HomeResponseDto", description = "홈 화면 응답 DTO")
@Getter
public class HomeResponseDto {
    @Schema(description = "테마 리스트")
    private List<ThemeDto.Response> themes;

    @Schema(description = "공지 리스트(배너)")
    private List<NoticeDto.Response> notices;

    @Schema(description = "새로운 알림 유무")
    private boolean hasNewNotifications;

    @Schema(description = "현재 참여중인 채팅방 ID")
    private long curChatRoomId;

    @Schema(description = "현재 참여중인 채팅방의 테마 ID")
    private long curThemeId;

    public HomeResponseDto(List<ThemeDto.Response> themes, List<NoticeDto.Response> notices, boolean hasNewNotifications, long curChatRoomId, long curThemeId) {
        this.themes = themes;
        this.notices = notices;
        this.hasNewNotifications = hasNewNotifications;
        this.curChatRoomId = curChatRoomId;
        this.curThemeId = curThemeId;
    }
}
