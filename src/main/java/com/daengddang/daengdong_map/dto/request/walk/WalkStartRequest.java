package com.daengddang.daengdong_map.dto.request.walk;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WalkStartRequest {

    @NotNull
    private Double startLat;

    @NotNull
    private Double startLng;
}
