package com.example.dice_talk.member.Dto;

import com.example.dice_talk.member.entity.DiceLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class DiceLogDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post {
        @NotBlank(message = "log 타입을 작성해주세요. 예시) 충전내역 / 사용내역 중 택 1 작성")
        private DiceLog.LogType logType;

        //타입 : 충전내역(=product), 사용내역(=item)
        @NotBlank(message = "타입의 상세 정보를 입력해주세요. " +
                "예시) 충전내역(type): 다이스 10개(product) or 사용내역(type): 채팅방 나가기(item)")
        private String info;

        @NotBlank
        private int quantity;

    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private Long logId;
        private DiceLog.LogType logType;
        private String info;
        private int quantity;
        private LocalDateTime createdAt;
    }
}
