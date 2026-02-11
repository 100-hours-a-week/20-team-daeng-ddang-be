package com.daengddang.daengdong_map.dto.response.ranking.region;

import java.util.List;
import lombok.Getter;

@Getter
public class RegionRankingListResponse {

    private final List<RegionRankItemResponse> ranks;
    private final String nextCursor;
    private final boolean hasNext;

    private RegionRankingListResponse(List<RegionRankItemResponse> ranks, String nextCursor, boolean hasNext) {
        this.ranks = ranks;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static RegionRankingListResponse of(List<RegionRankItemResponse> ranks, String nextCursor, boolean hasNext) {
        return new RegionRankingListResponse(ranks, nextCursor, hasNext);
    }
}
