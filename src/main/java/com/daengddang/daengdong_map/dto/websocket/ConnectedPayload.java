package com.daengddang.daengdong_map.dto.websocket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ConnectedPayload {

    private final Long walkId;
    private final LocalDateTime connectedAt;

    @Builder
    private ConnectedPayload(Long walkId, LocalDateTime connectedAt) {
        this.walkId = walkId;
        this.connectedAt = connectedAt;
    }

    public static ConnectedPayload of(Long walkId, LocalDateTime connectedAt) {
        return ConnectedPayload.builder()
                .walkId(walkId)
                .connectedAt(connectedAt)
                .build();
    }
}
