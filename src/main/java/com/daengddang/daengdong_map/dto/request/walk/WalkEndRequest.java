package com.daengddang.daengdong_map.dto.request.walk;

import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import jakarta.validation.constraints.NotNull;
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
}
