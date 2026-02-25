package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.region.RegionStatus;
import com.daengddang.daengdong_map.dto.response.region.RegionResponse;
import com.daengddang.daengdong_map.dto.response.user.RegionListResponse;
import com.daengddang.daengdong_map.repository.RegionRepository;
import com.daengddang.daengdong_map.service.cache.RegionCacheMetrics;
import com.daengddang.daengdong_map.service.cache.RegionCacheStore;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegionService {

    private static final Long ROOT_PARENT_KEY = -1L;

    private final RegionRepository regionRepository;
    private final RegionCacheStore regionCacheStore;
    private final RegionCacheMetrics regionCacheMetrics;
    private final ConcurrentHashMap<Long, CompletableFuture<RegionListResponse>> inFlightRegionLoads = new ConcurrentHashMap<>();

    public RegionListResponse getRegions(Long parentId) {
        Region parent = null;
        if (parentId != null) {
            parent = regionRepository
                    .findByIdAndStatus(parentId, RegionStatus.ACTIVE)
                    .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));
        }

        return getCachedOrLoad(parentId, parent);
    }

    public RegionResponse getRegion(Long regionId) {
        Region region = regionRepository.findById(regionId).orElseThrow(
                () -> new BaseException(ErrorCode.REGION_NOT_FOUND)
        );

        return RegionResponse.from(region);
    }

    private RegionListResponse getCachedOrLoad(Long parentId, Region parent) {
        Optional<RegionListResponse> cached = regionCacheStore.get(parentId);
        if (cached.isPresent()) {
            RegionListResponse response = cached.get();
            ensureNonEmptyForChild(parentId, response);
            return response;
        }

        Long inFlightKey = parentId == null ? ROOT_PARENT_KEY : parentId;
        while (true) {
            CompletableFuture<RegionListResponse> existing = inFlightRegionLoads.get(inFlightKey);
            if (existing != null) {
                RegionListResponse response = existing.join();
                ensureNonEmptyForChild(parentId, response);
                return response;
            }

            CompletableFuture<RegionListResponse> created = new CompletableFuture<>();
            if (inFlightRegionLoads.putIfAbsent(inFlightKey, created) == null) {
                try {
                    RegionListResponse response = loadFromDbAndCache(parentId, parent);
                    created.complete(response);
                    return response;
                } catch (Exception e) {
                    created.completeExceptionally(e);
                    throw e;
                } finally {
                    inFlightRegionLoads.remove(inFlightKey, created);
                }
            }
        }
    }

    private RegionListResponse loadFromDbAndCache(Long parentId, Region parent) {
        regionCacheMetrics.recordDbLoad();
        List<Region> regions = parentId == null
                ? regionRepository.findByParentIsNullAndStatusOrderByNameAsc(RegionStatus.ACTIVE)
                : regionRepository.findByParentAndStatusOrderByNameAsc(parent, RegionStatus.ACTIVE);
        RegionListResponse response = RegionListResponse.from(regions);
        ensureNonEmptyForChild(parentId, response);
        regionCacheStore.put(parentId, response);
        return response;
    }

    private void ensureNonEmptyForChild(Long parentId, RegionListResponse response) {
        if (parentId != null && response.getRegions().isEmpty()) {
            throw new BaseException(ErrorCode.REGION_NOT_FOUND);
        }
    }
}
