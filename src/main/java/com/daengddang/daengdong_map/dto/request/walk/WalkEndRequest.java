package com.daengddang.daengdong_map.dto.request.walk;

import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkPoint;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class WalkEndRequest {

    @NotNull
    private Double endLat;

    @NotNull
    private Double endLng;

    @NotNull
    private Double totalDistanceKm;

    @NotNull
    private Integer durationSeconds;

    @NotNull
    private WalkStatus status;

    public static WalkPoint of(WalkEndRequest dto, Walk walk, LocalDateTime recordedAt) {
        return WalkPoint.builder()
                .walk(walk)
                .latitude(dto.getEndLat())
                .longitude(dto.getEndLng())
                .recordedAt(recordedAt)
                .build();
    }
}
