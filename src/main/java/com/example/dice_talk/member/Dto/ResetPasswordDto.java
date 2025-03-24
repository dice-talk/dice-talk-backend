package com.example.dice_talk.member.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordDto {
    private String email;
    private String newPassword;
}
