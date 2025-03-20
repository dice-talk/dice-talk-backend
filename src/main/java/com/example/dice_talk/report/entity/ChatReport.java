package com.example.dice_talk.report.entity;

import com.example.dice_talk.audit.BaseEntity;
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
public class ChatReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatReportId;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    public void setReport(Report report){
        this.report = report;
        if(!report.getChatReports().contains(this)){
            report.setChatReport(this);
        }
    }
}
