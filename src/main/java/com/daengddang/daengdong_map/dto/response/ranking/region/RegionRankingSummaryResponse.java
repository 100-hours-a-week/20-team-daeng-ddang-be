package com.daengddang.daengdong_map.dto.response.ranking.region;

import java.util.List;
import lombok.Getter;

@Getter
public class RegionRankingSummaryResponse {

    private final List<RegionRankItemResponse> topRanks;
    private final RegionRankItemResponse myRank;

    private RegionRankingSummaryResponse(List<RegionRankItemResponse> topRanks, RegionRankItemResponse myRank) {
        this.topRanks = topRanks;
        this.myRank = myRank;
    }

    public static RegionRankingSummaryResponse of(List<RegionRankItemResponse> topRanks, RegionRankItemResponse myRank) {
        return new RegionRankingSummaryResponse(topRanks, myRank);
    }
}
