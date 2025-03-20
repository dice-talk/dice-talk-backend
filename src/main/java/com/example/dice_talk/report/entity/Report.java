package com.example.dice_talk.report.entity;

import com.example.dice_talk.chattingroom.entity.ChattingRoom;
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
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private String reason;

    @Column
    private String image;

    @Column(nullable = false)
    private Long reporterId;

    @OneToMany(mappedBy = "chatReport")
    private List<ChatReport> chatReports = new ArrayList<>();
}
