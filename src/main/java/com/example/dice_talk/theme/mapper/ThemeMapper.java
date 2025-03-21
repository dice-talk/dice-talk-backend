package com.example.dice_talk.theme.mapper;

import com.example.dice_talk.theme.dto.ThemeDto;
import com.example.dice_talk.theme.entity.Theme;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ThemeMapper {
    Theme themePostToTheme(ThemeDto.Post dto);

    Theme themePatchToTheme(ThemeDto.Patch dto);

    ThemeDto.Response themeToThemeResponse(Theme theme);

    List<ThemeDto.Response> themesToThemeResponses(List<Theme> themes);

}
