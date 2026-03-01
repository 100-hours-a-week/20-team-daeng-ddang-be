package com.daengddang.daengdong_map.service.cache;

import com.daengddang.daengdong_map.dto.response.dog.BreedListResponse;
import com.daengddang.daengdong_map.dto.response.dog.BreedResponse;
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
public class BreedCacheStore {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final CacheDefaultProperties defaultProperties;
    private final BreedCacheProperties properties;
    private final BreedCacheMetrics metrics;

    public Optional<BreedListResponse> getAll() {
        if (!isEnabled()) {
            metrics.recordBypassDisabled();
            return Optional.empty();
        }

        try {
            RBucket<String> bucket = redissonClient.getBucket(resolveCacheKey(), StringCodec.INSTANCE);
            String cachedJson = bucket.get();
            if (cachedJson == null || cachedJson.isBlank()) {
                metrics.recordMiss();
                return Optional.empty();
            }
            BreedCachePayload payload = objectMapper.readValue(cachedJson, BreedCachePayload.class);
            BreedListResponse cached = fromPayload(payload);
            metrics.recordHit();
            return Optional.of(cached);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("견종 캐시 조회 실패 (Breed cache read failed)", e);
            return Optional.empty();
        }
    }

    public void putAll(BreedListResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            String responseJson = objectMapper.writeValueAsString(toPayload(response));
            RBucket<String> bucket = redissonClient.getBucket(resolveCacheKey(), StringCodec.INSTANCE);
            bucket.setAsync(responseJson, resolveTtlSeconds(), TimeUnit.SECONDS)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            metrics.recordWriteError();
                            log.warn("견종 캐시 비동기 저장 실패 (Breed cache async write failed)", throwable);
                        }
                    });
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("견종 캐시 저장 실패 (Breed cache write failed)", e);
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

    private String resolveCacheKey() {
        String version = properties.getKeyVersion() == null
                ? defaultProperties.getKeyVersion()
                : properties.getKeyVersion();
        String key = properties.getKey();
        if (version == null || version.isBlank()) {
            return key;
        }
        return version + ":" + key;
    }

    private BreedCachePayload toPayload(BreedListResponse response) {
        List<BreedCacheItem> breeds = response.getBreeds().stream()
                .map(item -> new BreedCacheItem(item.getBreedId(), item.getName()))
                .toList();
        return new BreedCachePayload(breeds);
    }

    private BreedListResponse fromPayload(BreedCachePayload payload) {
        if (payload == null || payload.getBreeds() == null) {
            return BreedListResponse.of(List.of());
        }
        List<BreedResponse> breeds = payload.getBreeds().stream()
                .map(item -> BreedResponse.of(item.getBreedId(), item.getName()))
                .toList();
        return BreedListResponse.of(breeds);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class BreedCachePayload {
        private List<BreedCacheItem> breeds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class BreedCacheItem {
        private Long breedId;
        private String name;
    }
}
