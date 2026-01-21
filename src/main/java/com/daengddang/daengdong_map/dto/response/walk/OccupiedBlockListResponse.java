package com.daengddang.daengdong_map.dto.response.walk;

import java.util.List;
import lombok.Getter;

@Getter
public class OccupiedBlockListResponse {

    private final List<OccupiedBlockResponse> blocks;

    private OccupiedBlockListResponse(List<OccupiedBlockResponse> blocks) {
        this.blocks = blocks;
    }

    public static OccupiedBlockListResponse of(List<OccupiedBlockResponse> blocks) {
        return new OccupiedBlockListResponse(blocks);
    }
}
