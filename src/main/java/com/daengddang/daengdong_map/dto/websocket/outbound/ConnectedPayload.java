package com.daengddang.daengdong_map.dto.websocket.outbound;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ConnectedPayload {

    private final Long walkId;
    private final LocalDateTime connectedAt;

    @JsonCreator
    public ConnectedPayload(
            @JsonProperty("walkId") Long walkId,
            @JsonProperty("connectedAt") LocalDateTime connectedAt
    ) {
        this.walkId = walkId;
        this.connectedAt = connectedAt;
    }

    public static ConnectedPayload from(Long walkId, LocalDateTime connectedAt) {
        return new ConnectedPayload(walkId, connectedAt);
    }
}
