package com.daengddang.daengdong_map.dto.websocket;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BlockSyncEntry {

    private final String blockId;
    private final Long dogId;

    @Builder
    private BlockSyncEntry(String blockId, Long dogId) {
        this.blockId = blockId;
        this.dogId = dogId;
    }

    public static BlockSyncEntry of(String blockId, Long dogId) {
        return BlockSyncEntry.builder()
                .blockId(blockId)
                .dogId(dogId)
                .build();
    }
}
