package com.daengddang.daengdong_map.dto.websocket.outbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BlockOccupyFailedPayload {

    private final String reason;

    @JsonCreator
    public BlockOccupyFailedPayload(
            @JsonProperty("reason") BlockOccupyFailReason reason
    ) {
        this.reason = reason.getMessage();
    }

    public static BlockOccupyFailedPayload of(BlockOccupyFailReason reason) {
        return new BlockOccupyFailedPayload(reason);
    }
}
