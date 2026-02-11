package com.daengddang.daengdong_map.dto.response.ranking.contribution;

import lombok.Getter;

@Getter
public class RegionContributionRankItemResponse {

    private final Integer rank;
    private final Long dogId;
    private final String dogName;
    private final String profileImageUrl;
    private final Double dogDistance;
    private final Double contributionRate;

    private RegionContributionRankItemResponse(Integer rank,
                                               Long dogId,
                                               String dogName,
                                               String profileImageUrl,
                                               Double dogDistance,
                                               Double contributionRate) {
        this.rank = rank;
        this.dogId = dogId;
        this.dogName = dogName;
        this.profileImageUrl = profileImageUrl;
        this.dogDistance = dogDistance;
        this.contributionRate = contributionRate;
    }

    public static RegionContributionRankItemResponse of(Integer rank,
                                                        Long dogId,
                                                        String dogName,
                                                        String profileImageUrl,
                                                        Double dogDistance,
                                                        Double contributionRate) {
        return new RegionContributionRankItemResponse(rank, dogId, dogName, profileImageUrl, dogDistance, contributionRate);
    }
}
