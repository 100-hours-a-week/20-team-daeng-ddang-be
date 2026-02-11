package com.daengddang.daengdong_map.dto.request.ranking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingCursorRequest {

    private String cursor;

    @Min(1)
    @Max(100)
    private Integer limit;

    @Builder
    private RankingCursorRequest(String cursor, Integer limit) {
        this.cursor = cursor;
        this.limit = limit;
    }
}
