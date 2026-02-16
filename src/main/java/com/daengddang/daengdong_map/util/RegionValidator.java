package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.region.RegionStatus;
import com.daengddang.daengdong_map.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionValidator {

    private final RegionRepository regionRepository;

    public Region getActiveRegionOrThrow(Long regionId) {
        return regionRepository.findByIdAndStatus(regionId, RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));
    }

    public void validateActiveRegion(Long regionId) {
        getActiveRegionOrThrow(regionId);
    }
}
