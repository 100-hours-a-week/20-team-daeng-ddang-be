package com.daengddang.daengdong_map.dto.websocket;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BlockOccupyFailedPayload {

    private final BlockOccupyFailReason reason;

    @Builder
    private BlockOccupyFailedPayload(BlockOccupyFailReason reason) {
        this.reason = reason;
    }

    public static BlockOccupyFailedPayload of(BlockOccupyFailReason reason) {
        return BlockOccupyFailedPayload.builder()
                .reason(reason)
                .build();
    }
}
