package com.daengddang.daengdong_map.dto.response.footprint;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FootprintDiaryDetailResponse {

    private final Long walkDiaryId;
    private final LocalDateTime createdAt;
    private final String mapImageUrl;
    private final Double distance;
    private final Integer duration;
    private final String region;
    private final String memo;

    @Builder
    private FootprintDiaryDetailResponse(Long walkDiaryId,
                                         LocalDateTime createdAt,
                                         String mapImageUrl,
                                         Double distance,
                                         Integer duration,
                                         String region,
                                         String memo) {
        this.walkDiaryId = walkDiaryId;
        this.createdAt = createdAt;
        this.mapImageUrl = mapImageUrl;
        this.distance = distance;
        this.duration = duration;
        this.region = region;
        this.memo = memo;
    }

    public static FootprintDiaryDetailResponse of(Long walkDiaryId,
                                                  LocalDateTime createdAt,
                                                  String mapImageUrl,
                                                  Double distance,
                                                  Integer duration,
                                                  String region,
                                                  String memo) {
        return FootprintDiaryDetailResponse.builder()
                .walkDiaryId(walkDiaryId)
                .createdAt(createdAt)
                .mapImageUrl(mapImageUrl)
                .distance(distance)
                .duration(duration)
                .region(region)
                .memo(memo)
                .build();
    }
}
