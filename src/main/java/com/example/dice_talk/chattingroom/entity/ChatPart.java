package com.example.dice_talk.chattingroom.entity;

import com.example.dice_talk.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private String profile;

    @Enumerated(value = EnumType.STRING)
    private ExitStatus exitStatus = ExitStatus.MEMBER_ENTER;

    @ManyToOne
    @JoinColumn(name = "member-id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "room-id")
    private ChattingRoom room;

    public enum ExitStatus{
        MEMBER_ENTER("참여중"),
        MEMBER_EXIT("퇴장");

        @Getter
        private String status;

        ExitStatus(String status) {
            this.status = status;
        }
    }
}
