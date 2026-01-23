package com.daengddang.daengdong_map.dto.response.walk;

import java.time.LocalDateTime;

import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WalkStartResponse {

    private final Long walkId;
    private final LocalDateTime startedAt;
    private WalkStatus status;

    @Builder
    private WalkStartResponse(Long walkId, LocalDateTime startedAt, WalkStatus status) {
        this.walkId = walkId;
        this.startedAt = startedAt;
        this.status = status;
    }

    public static WalkStartResponse of(Long walkId, LocalDateTime startedAt, WalkStatus status) {
        return WalkStartResponse.builder()
                .walkId(walkId)
                .startedAt(startedAt)
                .status(status)
                .build();
    }
}
