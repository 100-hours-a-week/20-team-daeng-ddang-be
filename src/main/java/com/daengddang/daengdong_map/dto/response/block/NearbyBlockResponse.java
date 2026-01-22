package com.daengddang.daengdong_map.dto.response.block;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class NearbyBlockResponse {

    private final String blockId;
    private final Long dogId;
    private final LocalDateTime occupiedAt;

    @Builder
    private NearbyBlockResponse(String blockId, Long dogId, LocalDateTime occupiedAt) {
        this.blockId = blockId;
        this.dogId = dogId;
        this.occupiedAt = occupiedAt;
    }

    public static NearbyBlockResponse of(String blockId, Long dogId, LocalDateTime occupiedAt) {
        return NearbyBlockResponse.builder()
                .blockId(blockId)
                .dogId(dogId)
                .occupiedAt(occupiedAt)
                .build();
    }
}
