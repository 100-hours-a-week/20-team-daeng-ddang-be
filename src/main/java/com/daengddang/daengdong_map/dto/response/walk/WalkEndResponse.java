package com.daengddang.daengdong_map.dto.response.walk;

import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WalkEndResponse {

    private final Long walkId;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;
    private final Double totalDistanceKm;
    private final Integer durationSeconds;
    private final Integer occupiedBlockCount;
    private final WalkStatus status;

    @Builder
    private WalkEndResponse(Long walkId,
                            LocalDateTime startedAt,
                            LocalDateTime endedAt,
                            Double totalDistanceKm,
                            Integer durationSeconds,
                            Integer occupiedBlockCount,
                            WalkStatus status) {
        this.walkId = walkId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.totalDistanceKm = totalDistanceKm;
        this.durationSeconds = durationSeconds;
        this.occupiedBlockCount = occupiedBlockCount;
        this.status = status;
    }

    public static WalkEndResponse from(Long walkId,
                                     LocalDateTime startedAt,
                                     LocalDateTime endedAt,
                                     Double totalDistanceKm,
                                     Integer durationSeconds,
                                     Integer occupiedBlockCount,
                                     WalkStatus status) {
        return WalkEndResponse.builder()
                .walkId(walkId)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .totalDistanceKm(totalDistanceKm)
                .durationSeconds(durationSeconds)
                .occupiedBlockCount(occupiedBlockCount)
                .status(status)
                .build();
    }
}
