package com.daengddang.daengdong_map.dto.response.footprint;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FootprintCalendarDayResponse {

    private final LocalDate date;
    private final boolean hasWalkDiary;
    private final boolean hasHealthCare;
    private final int walkDiaryCount;
    private final int healthCareCount;

    @Builder
    private FootprintCalendarDayResponse(LocalDate date,
                                         boolean hasWalkDiary,
                                         boolean hasHealthCare,
                                         int walkDiaryCount,
                                         int healthCareCount) {
        this.date = date;
        this.hasWalkDiary = hasWalkDiary;
        this.hasHealthCare = hasHealthCare;
        this.walkDiaryCount = walkDiaryCount;
        this.healthCareCount = healthCareCount;
    }

    public static FootprintCalendarDayResponse of(LocalDate date, int walkDiaryCount, int healthCareCount) {
        return FootprintCalendarDayResponse.builder()
                .date(date)
                .hasWalkDiary(walkDiaryCount > 0)
                .hasHealthCare(healthCareCount > 0)
                .walkDiaryCount(walkDiaryCount)
                .healthCareCount(healthCareCount)
                .build();
    }
}
