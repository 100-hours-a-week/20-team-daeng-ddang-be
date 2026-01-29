package com.daengddang.daengdong_map.dto.websocket.outbound;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BlockTakenPayload {

    private final String blockId;
    private final Long previousDogId;
    private final Long newDogId;
    private final LocalDateTime takenAt;

    @JsonCreator
    public BlockTakenPayload(
            @JsonProperty("blockId") String blockId,
            @JsonProperty("previousDogId") Long previousDogId,
            @JsonProperty("newDogId") Long newDogId,
            @JsonProperty("takenAt") LocalDateTime takenAt
    ) {
        this.blockId = blockId;
        this.previousDogId = previousDogId;
        this.newDogId = newDogId;
        this.takenAt = takenAt;
    }

    public static BlockTakenPayload from(String blockId, Long previousDogId, Long newDogId, LocalDateTime takenAt) {
        return new BlockTakenPayload(blockId, previousDogId, newDogId, takenAt);
    }
}
