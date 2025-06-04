package com.example.dice_talk.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossCancelDto {
    private String orderId;
    private String cancelReason;
}
