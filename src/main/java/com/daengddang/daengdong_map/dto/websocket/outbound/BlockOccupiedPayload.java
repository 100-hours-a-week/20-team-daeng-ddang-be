package com.daengddang.daengdong_map.dto.websocket.outbound;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BlockOccupiedPayload {

    private final String blockId;
    private final Long dogId;
    private final String dogName;
    private final LocalDateTime occupiedAt;

    @JsonCreator
    public BlockOccupiedPayload(
            @JsonProperty("blockId") String blockId,
            @JsonProperty("dogId") Long dogId,
            @JsonProperty("dogName") String dogName,
            @JsonProperty("occupiedAt") LocalDateTime occupiedAt
    ) {
        this.blockId = blockId;
        this.dogId = dogId;
        this.dogName = dogName;
        this.occupiedAt = occupiedAt;
    }

    public static BlockOccupiedPayload from(String blockId, Long dogId, String dogName, LocalDateTime occupiedAt) {
        return new BlockOccupiedPayload(blockId, dogId, dogName, occupiedAt);
    }
}
