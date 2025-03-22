package com.example.dice_talk.theme.sevice;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.theme.repository.ThemeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme createTheme(Theme theme){
        // 테마 등록 후 반환
        return themeRepository.save(theme);
    }

    public Theme updateTheme(Theme theme){
        Theme findTheme =findVerifiedTheme(theme.getThemeId());
        // 변경 가능한 필드 확인 후 변경
        Optional.ofNullable(theme.getName())
                .ifPresent(themeName -> findTheme.setName(themeName));
        Optional.ofNullable(theme.getDescription())
                .ifPresent(description -> findTheme.setDescription(description));
        Optional.ofNullable(theme.getImage())
                .ifPresent(image -> findTheme.setImage(image));
        return themeRepository.save(findTheme);
    }

    public Theme findTheme(long themeId){
        return findVerifiedTheme(themeId);
    }

    public Page<Theme> findThemes(int page, int size){
        // page 번호 검증
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        // page 객체에 담아서 반환
        return themeRepository.findAll(PageRequest.of(page-1, size, Sort.by("themeId").descending()));
    }

    public void deleteTheme(long themeId){
        Theme theme = themeRepository.findById(themeId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.THEME_NOT_FOUND));
        theme.setThemeStatus(Theme.ThemeStatus.THEME_CLOSE);
        themeRepository.save(theme);
    }

    public Theme findVerifiedTheme(long themeId){
        // themeId로 DB에서 조회 후 없으면 예외 발생
        return themeRepository.findById(themeId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.THEME_NOT_FOUND));
    }
}
