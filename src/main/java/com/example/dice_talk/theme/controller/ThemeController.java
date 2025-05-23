package com.example.dice_talk.theme.controller;

import com.example.dice_talk.dto.ListResponseDto;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.item.dto.ItemDto;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.theme.dto.ThemeDto;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.theme.mapper.ThemeMapper;
import com.example.dice_talk.theme.sevice.ThemeService;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/themes")
@Validated
@Slf4j
public class ThemeController {
    private final static String THEME_DEFAULT_URL = "/themes";
    private final ThemeService themeService;
    private final ThemeMapper mapper;
    private final JsonParserUtil jsonParserUtil;

    public ThemeController(ThemeService themeService, ThemeMapper mapper, JsonParserUtil jsonParserUtil) {
        this.themeService = themeService;
        this.mapper = mapper;
        this.jsonParserUtil = jsonParserUtil;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity postTheme(@Valid @RequestParam String themePostDtoString,
                                    @RequestPart(value = "image", required = false)MultipartFile imageFile) throws IOException {
        ThemeDto.Post postDto = jsonParserUtil.parse(themePostDtoString, ThemeDto.Post.class);
        Theme createdTheme = themeService.createTheme(mapper.themePostToTheme(postDto), imageFile);
        URI location = UriCreator.createUri(THEME_DEFAULT_URL, createdTheme.getThemeId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping(value = "/{theme-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity patchTheme(
            @PathVariable("theme-id") @Positive long themeId,
            @Valid @RequestParam String themePatchDtoString,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        ThemeDto.Patch patchDto = jsonParserUtil.parse(themePatchDtoString, ThemeDto.Patch.class);
        patchDto.setThemeId(themeId);
        Theme theme = themeService.updateTheme(mapper.themePatchToTheme(patchDto), imageFile);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.themeToThemeResponse(theme)), HttpStatus.OK);
    }

    // 활성화 상태의 테마 목록 조회 (회원용)
    @GetMapping
    public ResponseEntity getThemes(){
        List<Theme> themes = themeService.findAllThemesOn();
        return new ResponseEntity<>(new ListResponseDto<>(mapper.themesToThemeResponses(themes)), HttpStatus.OK);
    }

    // 비활성화 상태의 테마까지 조회 (관리자용)
    @GetMapping("/admin")
    public ResponseEntity getAllThemes(@Positive @RequestParam int page, @Positive @RequestParam int size){
        Page<Theme> themePage = themeService.findThemes(page, size);
        List<Theme> themes = themePage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.themesToThemeResponses(themes), themePage), HttpStatus.OK);
    }

    @GetMapping("/{theme-id}")
    public ResponseEntity getTheme(@PathVariable("theme-id") @Positive long themeId){
        Theme theme = themeService.findTheme(themeId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.themeToThemeResponse(theme)), HttpStatus.OK);
    }

    @DeleteMapping("/{theme-id}")
    public ResponseEntity deleteTheme(@PathVariable("theme-id") long themeId){
        themeService.deleteTheme(themeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
