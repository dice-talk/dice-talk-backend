package com.example.dice_talk.theme.repository;

import com.example.dice_talk.theme.entity.Theme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAllByThemeStatus(Theme.ThemeStatus themeStatus);
    Page<Theme> findAllByThemeStatus (Theme.ThemeStatus status, Pageable pageable);

}
