package com.example.dice_talk.theme.repository;

import com.example.dice_talk.theme.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
