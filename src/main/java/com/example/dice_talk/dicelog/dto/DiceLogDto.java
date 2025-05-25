package com.example.dice_talk.dicelog.dto;

import com.example.dice_talk.dicelog.entity.DiceLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(name = "DiceLogDto", description = "다이스 사용/충전내역 DTO")
public class DiceLogDto {

    @Schema(name = "DiceLogPostDto", description = "다이스 사용/충전내역 등록 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post {

        @Schema(description = "수량", example = "30")
        @NotBlank
        private int quantity;

        @Schema(description = "로그 유형 (DICE_CHARGE: 충전, DICE_USED: 사용)", example = "DICE_CHARGE")
        @NotBlank(message = "log 타입을 작성해주세요. 예시) 충전내역 / 사용내역 중 택 1 작성")
        private DiceLog.LogType logType;

        //타입 : 충전내역(=product), 사용내역(=item)
        @Schema(description = "로그 상세 설명", example = "나가기 아이템 사용")
        @NotBlank(message = "타입의 상세 정보를 입력해주세요. " +
                "예시) 충전내역(type): 다이스 10개(product) or 사용내역(type): 채팅방 나가기(item)")
        private String info;

        @Schema(description = "회원 ID (서버에서 주입)", example = "1", hidden = true)
        private Long memberId;

        @Schema(description = "상품 ID (충전 시 필수)", example = "101")
        private Long productId;

        @Schema(description = "아이템 ID (사용 시 필수)", example = "7")
        private Long itemId;

    }

    @Schema(name = "DiceLogResponseDto", description = "다이스 사용/충전내역 응답 DTO")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class Response{
        @Schema(description = "다이스 로그 ID", example = "5")
        private Long logId;

        @Schema(description = "수량", example = "30")
        private int quantity;

        @Schema(description = "로그 유형", example = "DICE_USED")
        private DiceLog.LogType logType;

        @Schema(description = "로그 상세 설명", example = "초코 아이템 사용")
        private String info;

        @Schema(description = "회원 ID", example = "2")
        private Long memberId;

        @Schema(description = "상품 ID", example = "101")
        private Long productId;

        @Schema(description = "아이템 ID", example = "7")
        private Long itemId;

        @Schema(description = "로그 생성 일시", example = "2024-05-01T12:30:00")
        private LocalDateTime createdAt;
    }
}
