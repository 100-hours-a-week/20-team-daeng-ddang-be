package com.daengddang.daengdong_map.dto.request.walk;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkPoint;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class WalkStartRequest {

    @NotNull
    private Double startLat;

    @NotNull
    private Double startLng;

    public static Walk of(Dog dog, LocalDateTime startedAt) {
        return Walk.builder()
                .dog(dog)
                .startedAt(startedAt)
                .status(WalkStatus.IN_PROGRESS)
                .build();
    }

    public static WalkPoint of(WalkStartRequest dto, Walk walk, LocalDateTime recordedAt) {
        return WalkPoint.builder()
                .walk(walk)
                .latitude(dto.getStartLat())
                .longitude(dto.getStartLng())
                .recordedAt(recordedAt)
                .build();
    }
}
