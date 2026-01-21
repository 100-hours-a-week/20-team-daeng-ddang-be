package com.daengddang.daengdong_map.dto.websocket;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BlocksSyncPayload {

    private final List<BlockSyncEntry> blocks;

    @Builder
    private BlocksSyncPayload(List<BlockSyncEntry> blocks) {
        this.blocks = blocks;
    }

    public static BlocksSyncPayload of(List<BlockSyncEntry> blocks) {
        return BlocksSyncPayload.builder()
                .blocks(blocks)
                .build();
    }
}
