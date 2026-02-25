package com.daengddang.daengdong_map.dto.response.user;

import com.daengddang.daengdong_map.domain.region.Region;
import java.util.List;
import lombok.Getter;

@Getter
public class RegionListResponse {

    private final List<RegionResponse> regions;

    private RegionListResponse(List<RegionResponse> regions) {
        this.regions = regions;
    }

    public static RegionListResponse from(List<Region> regions) {
        List<RegionResponse> responses = regions.stream()
                .map(RegionResponse::from)
                .toList();
        return new RegionListResponse(responses);
    }

    public static RegionListResponse of(List<RegionResponse> regions) {
        return new RegionListResponse(regions);
    }
}
