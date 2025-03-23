package com.example.dice_talk.theme.repository;

import com.example.dice_talk.theme.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("SELECT t FROM Theme t WHERE t.themeStatus = :status")
    List<Theme> findAllByThemeStatus(@Param("status") Theme.ThemeStatus themeStatus);
}
