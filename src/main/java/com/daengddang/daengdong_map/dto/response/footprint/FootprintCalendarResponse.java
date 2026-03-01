package com.daengddang.daengdong_map.dto.response.footprint;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FootprintCalendarResponse {

    private final int year;
    private final int month;
    private final List<FootprintCalendarDayResponse> days;

    @Builder
    private FootprintCalendarResponse(int year, int month, List<FootprintCalendarDayResponse> days) {
        this.year = year;
        this.month = month;
        this.days = days;
    }

    public static FootprintCalendarResponse of(int year, int month, List<FootprintCalendarDayResponse> days) {
        return FootprintCalendarResponse.builder()
                .year(year)
                .month(month)
                .days(days)
                .build();
    }
}
