package com.daengddang.daengdong_map.dto.response.walk;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WalkStartResponse {

    private final Long walkId;
    private final LocalDateTime startedAt;

    @Builder
    private WalkStartResponse(Long walkId, LocalDateTime startedAt) {
        this.walkId = walkId;
        this.startedAt = startedAt;
    }

    public static WalkStartResponse of(Long walkId, LocalDateTime startedAt) {
        return WalkStartResponse.builder()
                .walkId(walkId)
                .startedAt(startedAt)
                .build();
    }
}
