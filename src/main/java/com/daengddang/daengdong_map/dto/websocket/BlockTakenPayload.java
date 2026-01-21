package com.daengddang.daengdong_map.dto.websocket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BlockTakenPayload {

    private final String blockId;
    private final Long previousDogId;
    private final Long newDogId;
    private final LocalDateTime takenAt;

    @Builder
    private BlockTakenPayload(String blockId, Long previousDogId, Long newDogId, LocalDateTime takenAt) {
        this.blockId = blockId;
        this.previousDogId = previousDogId;
        this.newDogId = newDogId;
        this.takenAt = takenAt;
    }

    public static BlockTakenPayload of(String blockId, Long previousDogId, Long newDogId, LocalDateTime takenAt) {
        return BlockTakenPayload.builder()
                .blockId(blockId)
                .previousDogId(previousDogId)
                .newDogId(newDogId)
                .takenAt(takenAt)
                .build();
    }
}
