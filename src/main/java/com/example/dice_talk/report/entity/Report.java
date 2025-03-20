package com.example.dice_talk.report.entity;

import com.example.dice_talk.audit.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private String reason;

    @Column
    private String image;

    @Column(nullable = false)
    private Long reporterId;

    @OneToMany(mappedBy = "report", cascade = CascadeType.PERSIST)
    private List<ChatReport> chatReports = new ArrayList<>();

    public void setChatReport(ChatReport chatReport){
        if(chatReport.getReport() != this){
            chatReport.setReport(this);
        }
        if(this.getChatReports().contains(chatReport)){
            this.getChatReports().add(chatReport);
        }
    }
}
