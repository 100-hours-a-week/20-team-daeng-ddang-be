package com.daengddang.daengdong_map.dto.websocket.outbound;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class WalkEndedPayload {

    private final Long walkId;
    private final LocalDateTime endedAt;

    @JsonCreator
    public WalkEndedPayload(
            @JsonProperty("walkId") Long walkId,
            @JsonProperty("endedAt") LocalDateTime endedAt
    ) {
        this.walkId = walkId;
        this.endedAt = endedAt;
    }

    public static WalkEndedPayload from(Long walkId, LocalDateTime endedAt) {
        return new WalkEndedPayload(walkId, endedAt);
    }
}
