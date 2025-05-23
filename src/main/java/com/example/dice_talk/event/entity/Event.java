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

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.EVENT_OPEN;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    public enum EventStatus{
        EVENT_OPEN("활성화"),
        EVENT_CLOSE("비활성화");

        @Getter
        private String status;

        EventStatus(String status) {
            this.status = status;
        }
    }
}
