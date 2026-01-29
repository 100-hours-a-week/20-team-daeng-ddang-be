package com.daengddang.daengdong_map.dto.websocket.outbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BlockSyncEntry {

    private final String blockId;
    private final Long dogId;

    @JsonCreator
    public BlockSyncEntry(
            @JsonProperty("blockId") String blockId,
            @JsonProperty("dogId") Long dogId
    ) {
        this.blockId = blockId;
        this.dogId = dogId;
    }

    public static BlockSyncEntry from(String blockId, Long dogId) {
        return new BlockSyncEntry(blockId, dogId);
    }
}
