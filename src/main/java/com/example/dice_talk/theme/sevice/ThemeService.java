package com.example.dice_talk.theme.sevice;

import com.example.dice_talk.aws.S3Uploader;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.theme.repository.ThemeRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final S3Uploader s3Uploader;

    public ThemeService(ThemeRepository themeRepository, S3Uploader s3Uploader) {
        this.themeRepository = themeRepository;
        this.s3Uploader = s3Uploader;
    }

    public Theme createTheme(Theme theme, MultipartFile file) throws IOException {
        AuthorizationUtils.verifyAdmin();
        // S3 업로드 후 Entity 에 Set
        if(file != null && !file.isEmpty()){
            String imageUrl = s3Uploader.upload(file, "theme-image");
            theme.setImage(imageUrl);
        }
        return themeRepository.save(theme);
    }

    public Theme updateTheme(Theme theme, MultipartFile file) throws IOException {
        AuthorizationUtils.verifyAdmin();

        Theme findTheme =findVerifiedTheme(theme.getThemeId());
        // 변경 가능한 필드 확인 후 변경
        Optional.ofNullable(theme.getName())
                .ifPresent(themeName -> findTheme.setName(themeName));
        Optional.ofNullable(theme.getDescription())
                .ifPresent(description -> findTheme.setDescription(description));
        if(file != null && !file.isEmpty()){
            String imageUrl = s3Uploader.upload(file, "theme-image");
            s3Uploader.moveToDeletedFolder(findTheme.getImage(), "deleted-theme-image");
            findTheme.setImage(imageUrl);
        }
        return themeRepository.save(findTheme);
    }

    public Theme findTheme(long themeId){
        return findVerifiedTheme(themeId);
    }

    // 관리자용 (비활성화 테마까지 조회)
    public Page<Theme> findThemes(int page, int size, Theme.ThemeStatus status){
        if (page < 1) throw new IllegalArgumentException("페이지는 1 이상이어야 합니다.");
        if (size < 1) throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");

        Pageable pageable = PageRequest.of(page-1, size, Sort.by("themeId").descending());

        if(status == null){
            return themeRepository.findAll(pageable);
        }
        return themeRepository.findAllByThemeStatus(status, pageable);
    }

    public void deleteTheme(long themeId){
        AuthorizationUtils.verifyAdmin();

        Theme theme = findVerifiedTheme(themeId);
        theme.setThemeStatus(Theme.ThemeStatus.THEME_CLOSE);
        themeRepository.save(theme);
    }

    public Theme findVerifiedTheme(long themeId){
        // themeId로 DB에서 조회 후 없으면 예외 발생
        return themeRepository.findById(themeId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.THEME_NOT_FOUND));
    }

    // 활성화된 테마 목록 조회 (회원용)
    public List<Theme> findAllThemesOn(){
        return themeRepository.findAllByThemeStatus(Theme.ThemeStatus.THEME_ON);
    }


}
