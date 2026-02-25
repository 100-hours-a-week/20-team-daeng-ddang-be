package com.daengddang.daengdong_map.service.cache;

import com.daengddang.daengdong_map.domain.region.RegionLevel;
import com.daengddang.daengdong_map.dto.response.user.RegionListResponse;
import com.daengddang.daengdong_map.dto.response.user.RegionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegionCacheStore {

    private static final String ROOT_PARENT_ID = "root";

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final CacheDefaultProperties defaultProperties;
    private final RegionCacheProperties properties;
    private final RegionCacheMetrics metrics;

    public Optional<RegionListResponse> get(Long parentId) {
        if (!isEnabled()) {
            metrics.recordBypassDisabled();
            return Optional.empty();
        }

        try {
            RBucket<String> bucket = redissonClient.getBucket(resolveCacheKey(parentId), StringCodec.INSTANCE);
            String cachedJson = bucket.get();
            if (cachedJson == null || cachedJson.isBlank()) {
                metrics.recordMiss();
                return Optional.empty();
            }
            RegionCachePayload payload = objectMapper.readValue(cachedJson, RegionCachePayload.class);
            RegionListResponse cached = fromPayload(payload);
            metrics.recordHit();
            return Optional.of(cached);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("지역 캐시 조회 실패 (Region cache read failed)", e);
            return Optional.empty();
        }
    }

    public void put(Long parentId, RegionListResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            String responseJson = objectMapper.writeValueAsString(toPayload(response));
            RBucket<String> bucket = redissonClient.getBucket(resolveCacheKey(parentId), StringCodec.INSTANCE);
            bucket.setAsync(responseJson, resolveTtlSeconds(), TimeUnit.SECONDS)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            metrics.recordWriteError();
                            log.warn("지역 캐시 비동기 저장 실패 (Region cache async write failed)", throwable);
                        }
                    });
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("지역 캐시 저장 실패 (Region cache write failed)", e);
        }
    }

    private long resolveTtlSeconds() {
        long base = properties.getTtlSeconds() == null
                ? defaultProperties.getTtlSeconds()
                : properties.getTtlSeconds();
        int jitterPercent = properties.getJitterPercent() == null
                ? defaultProperties.getJitterPercent()
                : properties.getJitterPercent();
        jitterPercent = Math.max(0, Math.min(100, jitterPercent));
        if (jitterPercent == 0 || base <= 0) {
            return Math.max(1, base);
        }

        double ratio = jitterPercent / 100.0;
        double min = base * (1 - ratio);
        double max = base * (1 + ratio);
        long ttl = Math.round(ThreadLocalRandom.current().nextDouble(min, max));
        return Math.max(1, ttl);
    }

    private boolean isEnabled() {
        return properties.getEnabled() == null
                ? defaultProperties.isEnabled()
                : properties.getEnabled();
    }

    private String resolveCacheKey(Long parentId) {
        String version = properties.getKeyVersion() == null
                ? defaultProperties.getKeyVersion()
                : properties.getKeyVersion();
        String parentKey = parentId == null ? ROOT_PARENT_ID : String.valueOf(parentId);
        String key = properties.getKey() + ":parent:" + parentKey;
        if (version == null || version.isBlank()) {
            return key;
        }
        return version + ":" + key;
    }

    private RegionCachePayload toPayload(RegionListResponse response) {
        List<RegionCacheItem> regions = response.getRegions().stream()
                .map(item -> new RegionCacheItem(
                        item.getRegionId(),
                        item.getName(),
                        item.getLevel(),
                        item.getParentRegionId()
                ))
                .toList();
        return new RegionCachePayload(regions);
    }

    private RegionListResponse fromPayload(RegionCachePayload payload) {
        if (payload == null || payload.getRegions() == null) {
            return RegionListResponse.of(List.of());
        }
        List<RegionResponse> regions = payload.getRegions().stream()
                .map(item -> RegionResponse.of(
                        item.getRegionId(),
                        item.getName(),
                        item.getLevel(),
                        item.getParentRegionId()
                ))
                .toList();
        return RegionListResponse.of(regions);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RegionCachePayload {
        private List<RegionCacheItem> regions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RegionCacheItem {
        private Long regionId;
        private String name;
        private RegionLevel level;
        private Long parentRegionId;
    }
}
