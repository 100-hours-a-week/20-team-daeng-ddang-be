package com.daengddang.daengdong_map.dto.response.ranking.region;

import lombok.Getter;

@Getter
public class RegionRankItemResponse {

    private final Integer rank;
    private final Long regionId;
    private final String regionName;
    private final Double totalDistance;

    private RegionRankItemResponse(Integer rank, Long regionId, String regionName, Double totalDistance) {
        this.rank = rank;
        this.regionId = regionId;
        this.regionName = regionName;
        this.totalDistance = totalDistance;
    }

    public static RegionRankItemResponse of(Integer rank, Long regionId, String regionName, Double totalDistance) {
        return new RegionRankItemResponse(rank, regionId, regionName, totalDistance);
    }
}
