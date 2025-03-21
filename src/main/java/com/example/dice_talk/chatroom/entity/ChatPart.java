package com.example.dice_talk.chatroom.entity;

import com.example.dice_talk.audit.BaseEntity;
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
public class ChatPart extends BaseEntity {
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
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    public void setMember(Member member){
        this.member = member;
        if(!member.getChatParts().contains(this)){
            member.setChatPart(this);
        }
    }

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
