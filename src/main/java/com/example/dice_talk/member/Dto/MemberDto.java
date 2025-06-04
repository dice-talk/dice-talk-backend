package com.example.dice_talk.member.Dto;

import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.report.dto.ReportDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

public class MemberDto {

    @Schema(name = "MemberPostDto", description = "회원 가입 DTO")
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class Post {
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email
        private String email;

        /*정규식 설명 :
           (?=.*[a-zA-Z]) : 비밀번호에 적어도 하나의 영문 대, 소문자가 포함되어야 한다.
           (?=.*[~!@#$%^&*+=()-_]) : 비밀번호에 적어도 하나의 특수문자가 포함되어야 한다.
           (?=.*[0-9]) : 비밀번호에 적어도 하나의 숫자가 포함되어야 한다.
           {8,16} : 비밀번호는 8~16자여야 한다.
       */
        @Schema(description = "비밀번호", example = "Abc123!@#")
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[~!@#$%^&*+=()_-])(?=.*[0-9]).{8,16}$",
                message = "비밀번호는 8~16자 영문 대, 소문자, 숫자, 특수문자를 사용하세요.")
        private String password;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "생년월일", example = "1990-01-01")
        private String birth;

        @Schema(description = "성별", example = "MALE")
        private Member.Gender gender;

        @Schema(description = "지역", example = "Seoul")
        private String region;

    }

    @Schema(name = "MyInfoResponseDto", description = "내 정보 조회 응답 DTO")
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MyInfoResponse {
        @Schema(description = "회원 ID", example = "1")
        private Long memberId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "생년월일", example = "1990-01-01")
        private String birth;

        @Schema(description = "성별", example = "FEMALE")
        private Member.Gender gender;

        @Schema(description = "지역", example = "서울시 강남구")
        private String region;

        @Schema(description = "다이스 보유량", example = "42")
        private int totalDice;

        @Schema(description = "회원 상태", example = "MEMBER_ACTIVE")
        private Member.MemberStatus memberStatus;

        @Schema(description = "나이대", example = "20대")
        private String ageGroup;

        @Schema(description = "알림 동의 여부", example = "true")
        private String notification;
    }

    @Schema(name = "MyPageResponseDto", description = "마이 페이지 조회 응답 DTO (익명 닉네임)")
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class MyPageResponse {
        @Schema(description = "회원 ID", example = "1")
        private long memberId;

        @Schema(description = "채팅방 닉네임", example = "네모지만 부드러운 네몽")
        private String nickname;

        @Schema(description = "채팅방 참여 상태", example = "ROOM_ENTER")
        private RoomParticipation exitStatus;

        @Schema(description = "나이대", example = "20대")
        private String ageGroup;

        @Schema(description = "다이스 보유량", example = "42")
        private int totalDice;

        public enum RoomParticipation {
            ROOM_ENTER("참가중"),
            ROOM_EXIT("퇴장");

            @Getter
            private String status;

            RoomParticipation(String status) {
                this.status = status;
            }
        }
    }

    @Schema(name = "DeletedMemberResponseDto", description = "탈퇴 회원 응답 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeletedMemberResponse {
        @Schema(description = "회원 ID", example = "1")
        private Long memberId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "생년월일", example = "1998-12-25")
        private String birth;

        @Schema(description = "지역", example = "서울시 강남구")
        private String region;

        @Schema(description = "탈퇴 사유", example = "서비스 이용 불편")
        private String deleteReason;

        @Schema(description = "나이대", example = "20대")
        private String ageGroup;

        @Schema(description = "성별", example = "MAIL")
        private Member.Gender gender;

        @Schema(description = "탈퇴 일시", example = "2024-03-20T14:30:00")
        private LocalDateTime deletedAt;
    }

    @Schema(name = "BannedMemberResponseDto", description = "정지 회원 상세 조회 응답 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BannedMemberResponse {
        @Schema(description = "회원 ID", example = "1")
        private Long memberId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "생년월일", example = "1998-12-25")
        private String birth;

        @Schema(description = "지역", example = "서울시 강남구")
        private String region;

        @Schema(description = "성별", example = "MALE")
        private Member.Gender gender;

        @Schema(description = "회원 상태", example = "MEMBER_ACTIVE")
        private Member.MemberStatus memberStatus;

        @Schema(description = "나이대", example = "20대")
        private String ageGroup;

        @Schema(description = "신고 당한 내역")
        private List<ReportDto.Response> reports;
    }

    @Schema(name = "BannedMemberListResponseDto", description = "정지 회원 목록 조회 응답 DTO")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BannedMemberListResponse {
        @Schema(description = "회원 ID", example = "1")
        private Long memberId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "생년월일", example = "1998-12-25")
        private String birth;

        @Schema(description = "지역", example = "서울시 강남구")
        private String region;

        @Schema(description = "성별", example = "MALE")
        private Member.Gender gender;

        @Schema(description = "회원 상태", example = "MEMBER_ACTIVE")
        private Member.MemberStatus memberStatus;

        @Schema(description = "나이대", example = "20대")
        private String ageGroup;

        @Schema(description = "정지일", example = "2025-06-05T12:00")
        private LocalDateTime bannedAt;
    }
}
