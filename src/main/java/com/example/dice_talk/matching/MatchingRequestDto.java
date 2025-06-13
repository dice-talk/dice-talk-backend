package com.example.dice_talk.matching;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MatchingRequestDto {
    private int themeId;
    private String region;
    private String ageGroup;
}
