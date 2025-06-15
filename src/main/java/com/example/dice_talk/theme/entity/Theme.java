package com.example.dice_talk.theme.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.event.entity.Event;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Theme extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long themeId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column
    private String description;

    @Column
    private String image;

    @Enumerated(value = EnumType.STRING)
    private ThemeStatus themeStatus = ThemeStatus.THEME_ON;

    public enum ThemeStatus{
        THEME_PLANNED("진행 예정"),
        THEME_ON("진행중"),
        THEME_CLOSE("종료");

        @Getter
        private String status;

        ThemeStatus(String status) {
            this.status = status;
        }
    }

    @OneToMany(mappedBy = "theme")
    private List<Event> events = new ArrayList<>();
}
