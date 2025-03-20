package com.example.dice_talk.report.entity;

import com.example.dice_talk.chat.entity.Chat;
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
public class ChatReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatReportId;

    @ManyToOne
    @JoinColumn(name = "report-id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "chat-id")
    private Chat chat;
}
