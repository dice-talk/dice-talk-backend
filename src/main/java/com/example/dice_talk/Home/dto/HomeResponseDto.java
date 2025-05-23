package com.example.dice_talk.Home.dto;

import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.theme.dto.ThemeDto;
import lombok.Getter;

import java.util.List;

@Getter
public class HomeResponseDto {
    private List<ThemeDto.Response> themes;
    private List<NoticeDto.Response> notices;
    private boolean hasNewNotifications;

    public HomeResponseDto(List<ThemeDto.Response> themes, List<NoticeDto.Response> notices, boolean hasNewNotifications) {
        this.themes = themes;
        this.notices = notices;
        this.hasNewNotifications = hasNewNotifications;
    }
}
