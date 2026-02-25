package com.daengddang.daengdong_map.dto.response.user;

import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.region.RegionLevel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class RegionResponse {

    private final Long regionId;
    private final String name;
    private final RegionLevel level;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Long parentRegionId;

    private RegionResponse(Long regionId, String name, RegionLevel level, Long parentRegionId) {
        this.regionId = regionId;
        this.name = name;
        this.level = level;
        this.parentRegionId = parentRegionId;
    }

    public static RegionResponse from(Region region) {
        Long parentId = region.getParent() == null ? null : region.getParent().getId();
        return new RegionResponse(region.getId(), region.getName(), region.getLevel(), parentId);
    }

    public static RegionResponse of(Long regionId, String name, RegionLevel level, Long parentRegionId) {
        return new RegionResponse(regionId, name, level, parentRegionId);
    }
}
