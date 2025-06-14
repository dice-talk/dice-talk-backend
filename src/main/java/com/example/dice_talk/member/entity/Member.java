package com.example.dice_talk.member.entity;

import com.example.dice_talk.audit.BaseEntity;
import com.example.dice_talk.chatroom.entity.ChatPart;
import com.example.dice_talk.dicelog.entity.DiceLog;
import com.example.dice_talk.question.entity.Question;
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
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(value = EnumType.STRING)
    private Gender gender = Gender.MALE;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private String region;

    // 일반 VARCHAR 컬럼이기 때문에, insertable/updatable 제어 불필요 (생성, 수정 시 직접 건드리지 않기 떄문에)
    @Column(name = "age_group", length = 10)
    private String ageGroup;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;

    @Column(nullable = false)
    private boolean notification;

    @Column(nullable = false)
    private int totalDice;

    @Column(nullable = false)
    private String ci;  //사용자 CI 저장하는 필드

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<ChatPart> chatParts = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<DiceLog> diceLogs = new ArrayList<>();

    public void setQuestion(Question question){
        if(question.getMember() != this){
            question.setMember(this);
        }
        if(!this.questions.contains(question)){
            questions.add(question);
        }
    }

    public void setDiceLog(DiceLog diceLog){
        if(diceLog.getMember() != this){
            diceLog.setMember(this);
        }
        if(!this.diceLogs.contains(diceLog)){
            diceLogs.add(diceLog);
        }
    }

    public void setChatPart(ChatPart chatPart){
        if(chatPart.getMember() != this){
            chatPart.setMember(this);
        }
        if(!this.chatParts.contains(this)){
            this.chatParts.add(chatPart);
        }
    }

    //member 상태변경
    public void deactivate(){
        this.memberStatus = MemberStatus.MEMBER_DELETED;
        this.questions.forEach(question -> question.deactivate());
    }

    public enum MemberStatus {
        MEMBER_ACTIVE("일반 회원"),
        MEMBER_SLEEP("휴면 회원"),
        MEMBER_BANNED("정지 회원"),
        MEMBER_DELETED("탈퇴 회원");

        @Getter
        private String status;

        MemberStatus(String status) {
            this.status = status;
        }
    }

    public enum Gender {
        MALE("남자"),
        FEMALE("여자");
        @Getter
        private String gender;

        Gender(String gender) {
        }
    }


}
