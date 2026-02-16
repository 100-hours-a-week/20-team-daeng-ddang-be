package com.daengddang.daengdong_map.dto.request.ranking;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingPeriodRequest {

    @NotBlank
    private String periodType;

    @NotBlank
    private String periodValue;

    @Builder
    private RankingPeriodRequest(String periodType, String periodValue) {
        this.periodType = periodType;
        this.periodValue = periodValue;
    }
}
