package com.example.dice_talk.member.Dto;

import com.example.dice_talk.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class MemberDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class Post{
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email
        private String email;

        /*정규식 설명 :
           (?=.*[a-zA-Z]) : 비밀번호에 적어도 하나의 영문 대, 소문자가 포함되어야 한다.
           (?=.*[~!@#$%^&*+=()-_]) : 비밀번호에 적어도 하나의 특수문자가 포함되어야 한다.
           (?=.*[0-9]) : 비밀번호에 적어도 하나의 숫자가 포함되어야 한다.
           {8,16} : 비밀번호는 8~16자여야 한다.
       */
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[~!@#$%^&*+=()_-])(?=.*[0-9])+$.{8,16}",
                message = "비밀번호는 8~16자 영문 대, 소문자, 숫자, 특수문자를 사용하세요.")
        private String password;

        @NotBlank
        @Pattern(regexp = "^010-\\d{3,4}-\\d{4}$", message = "휴대폰 번호는 010으로 시작되는 11자리 숫자와 '-'로 구성되어야 합니다. 예시)010-1234-5678")
        private String phone;

        private String name;
        private String birth;
        private Member.Gender gender;
        private String region;

    }


    @AllArgsConstructor
    @Getter
    public static class Response{

    }

}
