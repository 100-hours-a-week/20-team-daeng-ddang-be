package com.daengddang.daengdong_map.dto.websocket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WalkEndedPayload {

    private final Long walkId;
    private final LocalDateTime endedAt;

    @Builder
    private WalkEndedPayload(Long walkId, LocalDateTime endedAt) {
        this.walkId = walkId;
        this.endedAt = endedAt;
    }

    public static WalkEndedPayload of(Long walkId, LocalDateTime endedAt) {
        return WalkEndedPayload.builder()
                .walkId(walkId)
                .endedAt(endedAt)
                .build();
    }
}
