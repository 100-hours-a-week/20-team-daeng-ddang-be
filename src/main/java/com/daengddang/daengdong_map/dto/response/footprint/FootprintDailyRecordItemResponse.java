package com.daengddang.daengdong_map.dto.response.footprint;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FootprintDailyRecordItemResponse {

    private final FootprintRecordType type;
    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;

    @Builder
    private FootprintDailyRecordItemResponse(FootprintRecordType type, Long id, String title, LocalDateTime createdAt) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public static FootprintDailyRecordItemResponse of(FootprintRecordType type, Long id, String title,
                                                       LocalDateTime createdAt) {
        return FootprintDailyRecordItemResponse.builder()
                .type(type)
                .id(id)
                .title(title)
                .createdAt(createdAt)
                .build();
    }
}
