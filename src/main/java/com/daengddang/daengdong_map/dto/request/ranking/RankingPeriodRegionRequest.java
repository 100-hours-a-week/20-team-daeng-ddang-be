package com.daengddang.daengdong_map.dto.request.ranking;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingPeriodRegionRequest {

    @NotBlank
    private String periodType;

    @NotBlank
    private String periodValue;

    private Long regionId;

    @Builder
    private RankingPeriodRegionRequest(String periodType, String periodValue, Long regionId) {
        this.periodType = periodType;
        this.periodValue = periodValue;
        this.regionId = regionId;
    }
}
