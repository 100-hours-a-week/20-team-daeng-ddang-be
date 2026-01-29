package com.daengddang.daengdong_map.dto.response.block;

import java.util.List;
import lombok.Getter;

@Getter
public class NearbyBlockListResponse {

    private final List<NearbyBlockResponse> blocks;

    private NearbyBlockListResponse(List<NearbyBlockResponse> blocks) {
        this.blocks = blocks;
    }

    public static NearbyBlockListResponse from(List<NearbyBlockResponse> blocks) {
        return new NearbyBlockListResponse(blocks);
    }
}
