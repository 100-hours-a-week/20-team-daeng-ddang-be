package com.daengddang.daengdong_map.dto.response.footprint;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FootprintDailyRecordsResponse {

    private final LocalDate date;
    private final List<FootprintDailyRecordItemResponse> records;

    @Builder
    private FootprintDailyRecordsResponse(LocalDate date, List<FootprintDailyRecordItemResponse> records) {
        this.date = date;
        this.records = records;
    }

    public static FootprintDailyRecordsResponse of(LocalDate date, List<FootprintDailyRecordItemResponse> records) {
        return FootprintDailyRecordsResponse.builder()
                .date(date)
                .records(records)
                .build();
    }
}
