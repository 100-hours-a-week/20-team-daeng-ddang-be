package com.daengddang.daengdong_map.dto.response.ranking.contribution;

import java.util.List;
import lombok.Getter;

@Getter
public class RegionContributionRankingListResponse {

    private final List<RegionContributionRankItemResponse> ranks;
    private final String nextCursor;
    private final boolean hasNext;

    private RegionContributionRankingListResponse(List<RegionContributionRankItemResponse> ranks,
                                                  String nextCursor,
                                                  boolean hasNext) {
        this.ranks = ranks;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static RegionContributionRankingListResponse of(List<RegionContributionRankItemResponse> ranks,
                                                           String nextCursor,
                                                           boolean hasNext) {
        return new RegionContributionRankingListResponse(ranks, nextCursor, hasNext);
    }
}
