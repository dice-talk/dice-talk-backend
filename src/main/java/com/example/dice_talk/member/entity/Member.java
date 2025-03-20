package com.example.dice_talk.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 13, nullable = false, unique = true)
    private String phone;

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

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;

    @Column(nullable = false)
    private boolean notification;

    @Column(nullable = false)
    private int totalDice;

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
