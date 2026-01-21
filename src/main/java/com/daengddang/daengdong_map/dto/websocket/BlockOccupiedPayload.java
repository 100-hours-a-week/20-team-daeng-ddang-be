package com.daengddang.daengdong_map.dto.websocket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BlockOccupiedPayload {

    private final String blockId;
    private final Long dogId;
    private final String dogName;
    private final LocalDateTime occupiedAt;

    @Builder
    private BlockOccupiedPayload(String blockId, Long dogId, String dogName, LocalDateTime occupiedAt) {
        this.blockId = blockId;
        this.dogId = dogId;
        this.dogName = dogName;
        this.occupiedAt = occupiedAt;
    }

    public static BlockOccupiedPayload of(String blockId, Long dogId, String dogName, LocalDateTime occupiedAt) {
        return BlockOccupiedPayload.builder()
                .blockId(blockId)
                .dogId(dogId)
                .dogName(dogName)
                .occupiedAt(occupiedAt)
                .build();
    }
}
