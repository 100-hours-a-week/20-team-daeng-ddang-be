package com.daengddang.daengdong_map.dto.response.ranking.contribution;

import java.util.List;
import lombok.Getter;

@Getter
public class RegionContributionRankingSummaryResponse {

    private final List<RegionContributionRankItemResponse> topRanks;
    private final RegionContributionRankItemResponse myRank;

    private RegionContributionRankingSummaryResponse(List<RegionContributionRankItemResponse> topRanks,
                                                     RegionContributionRankItemResponse myRank) {
        this.topRanks = topRanks;
        this.myRank = myRank;
    }

    public static RegionContributionRankingSummaryResponse of(List<RegionContributionRankItemResponse> topRanks,
                                                              RegionContributionRankItemResponse myRank) {
        return new RegionContributionRankingSummaryResponse(topRanks, myRank);
    }
}
