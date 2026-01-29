package com.daengddang.daengdong_map.dto.response.walk;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OccupiedBlockResponse {

    private final String blockId;
    private final Long dogId;
    private final LocalDateTime occupiedAt;

    @Builder
    private OccupiedBlockResponse(String blockId, Long dogId, LocalDateTime occupiedAt) {
        this.blockId = blockId;
        this.dogId = dogId;
        this.occupiedAt = occupiedAt;
    }

    public static OccupiedBlockResponse from(String blockId, Long dogId, LocalDateTime occupiedAt) {
        return OccupiedBlockResponse.builder()
                .blockId(blockId)
                .dogId(dogId)
                .occupiedAt(occupiedAt)
                .build();
    }
}
