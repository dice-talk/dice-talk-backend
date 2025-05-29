package com.example.dice_talk.chatroom.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.member.entity.Member;
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
public class ExitLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long exitLogId;

    @Column(nullable = false)
    private Long memberId;
}
