package com.example.dice_talk.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class DashboardQuestion {

        @Schema(description = "제목", example = "회원가입 버튼 클릭이 안됩니다.")
        private String title;
        @Schema(description = "미답변 문의글 수", example = "5")
        private int noAnswerQuestionCount;
        @Schema(description = "주간 등록된 문의글 수", example = "17")
        private int weeklyQuestionCount;

}
