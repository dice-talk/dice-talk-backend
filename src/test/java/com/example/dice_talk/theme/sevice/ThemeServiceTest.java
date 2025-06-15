package com.example.dice_talk.theme.sevice;

import com.example.dice_talk.aws.S3Uploader;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.theme.repository.ThemeRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
// BDDMockito
import static org.mockito.BDDMockito.*;
// AssertJ
import static org.assertj.core.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private ThemeService themeService;

    private Theme theme;

    @BeforeEach
    void setup() {
        theme = new Theme();
        theme.setThemeId(1L);
        theme.setName("테스트테마");
        theme.setDescription("테스트 설명");
        theme.setThemeStatus(Theme.ThemeStatus.THEME_ON);
        theme.setImage("image-url");
    }

    @Test
    void createTheme_withoutFile() throws IOException {
        try(MockedStatic<AuthorizationUtils> mockedStatic = Mockito.mockStatic(AuthorizationUtils.class)){
        given(themeRepository.save(any(Theme.class))).willReturn(theme);

        Theme result = themeService.createTheme(theme, null);

        then(themeRepository).should().save(any(Theme.class));
        assertThat(result.getThemeId()).isEqualTo(1L);
        }
    }

    @Test
    void createTheme_withFile() throws IOException {
        try (MockedStatic<AuthorizationUtils> mockedStatic = Mockito.mockStatic(AuthorizationUtils.class)) {
            mockedStatic.when(AuthorizationUtils::verifyAdmin).thenAnswer(invocation -> null);

            var file = mock(org.springframework.web.multipart.MultipartFile.class);
            given(file.isEmpty()).willReturn(false);
            given(s3Uploader.upload(any(), eq("theme-image"))).willReturn("uploaded-url");
            given(themeRepository.save(any(Theme.class))).willReturn(theme);

            Theme result = themeService.createTheme(new Theme(), file);

            then(s3Uploader).should().upload(any(), eq("theme-image"));
            then(themeRepository).should().save(any(Theme.class));
        }
    }

    @Test
    void updateTheme_success() throws IOException {
        try (MockedStatic<AuthorizationUtils> mockedStatic = Mockito.mockStatic(AuthorizationUtils.class)) {
            mockedStatic.when(AuthorizationUtils::verifyAdmin).thenAnswer(invocation -> null);

            var file = mock(org.springframework.web.multipart.MultipartFile.class);
            given(file.isEmpty()).willReturn(false);
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(s3Uploader.upload(any(), eq("theme-image"))).willReturn("uploaded-url");
            given(themeRepository.save(any(Theme.class))).willReturn(theme);

            Theme updateTheme = new Theme();
            updateTheme.setThemeId(1L);
            updateTheme.setName("수정");
            updateTheme.setDescription("수정설명");

            Theme result = themeService.updateTheme(updateTheme, file);

            then(s3Uploader).should().upload(any(), eq("theme-image"));
            then(themeRepository).should().save(any(Theme.class));
        }
    }

    @Test
    void findTheme_exist() {
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        Theme result = themeService.findTheme(1L);

        assertThat(result.getName()).isEqualTo("테스트테마");
    }

    @Test
    void findTheme_notExist() {
        given(themeRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.findTheme(1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining(ExceptionCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    void findThemes_statusNull() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("themeId").descending());
        Page<Theme> page = new PageImpl<>(List.of(theme));
        given(themeRepository.findAll(pageable)).willReturn(page);

        Page<Theme> result = themeService.findThemes(1, 10, null);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findThemes_withStatus() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("themeId").descending());
        Page<Theme> page = new PageImpl<>(List.of(theme));
        given(themeRepository.findAllByThemeStatus(Theme.ThemeStatus.THEME_ON, pageable)).willReturn(page);

        Page<Theme> result = themeService.findThemes(1, 10, Theme.ThemeStatus.THEME_ON);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void deleteTheme_success() {
        try (MockedStatic<AuthorizationUtils> mockedStatic = Mockito.mockStatic(AuthorizationUtils.class)) {
            mockedStatic.when(AuthorizationUtils::verifyAdmin).thenAnswer(invocation -> null);

            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(themeRepository.save(any(Theme.class))).willReturn(theme);

            themeService.deleteTheme(1L);

            then(themeRepository).should().save(any(Theme.class));
        }
    }

    @Test
    void findAllThemesNotClose_success() {
        given(themeRepository.findAllByThemeStatusNot(Theme.ThemeStatus.THEME_CLOSE))
                .willReturn(List.of(theme));

        List<Theme> result = themeService.findAllThemesNotClose();

        assertThat(result).hasSize(1);
    }
}