package com.example.dice_talk.event.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.theme.entity.Theme;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false)
    private String eventName;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;
}
