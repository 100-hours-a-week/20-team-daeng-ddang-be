package com.daengddang.daengdong_map.dto.websocket.outbound;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class BlocksSyncPayload {

    private final List<BlockSyncEntry> blocks;

    @JsonCreator
    public BlocksSyncPayload(
            @JsonProperty("blocks") List<BlockSyncEntry> blocks
    ) {
        this.blocks = blocks;
    }

    public static BlocksSyncPayload from(List<BlockSyncEntry> blocks) {
        return new BlocksSyncPayload(blocks);
    }
}
