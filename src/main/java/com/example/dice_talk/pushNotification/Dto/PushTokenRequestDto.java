package com.example.dice_talk.pushNotification.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PushTokenRequestDto {
    private String expoPushToken;
    private String deviceType;
}
