package com.daengddang.daengdong_map.service.cache;

import com.daengddang.daengdong_map.domain.mission.MissionDifficulty;
import com.daengddang.daengdong_map.dto.response.mission.MissionListResponse;
import com.daengddang.daengdong_map.dto.response.mission.MissionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
public class MissionCacheStore {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final CacheDefaultProperties defaultProperties;
    private final MissionCacheProperties properties;
    private final MissionCacheMetrics metrics;

    public Optional<MissionListResponse> get() {
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
            MissionCachePayload payload = objectMapper.readValue(cachedJson, MissionCachePayload.class);
            MissionListResponse cached = fromPayload(payload);
            metrics.recordHit();
            return Optional.of(cached);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("미션 캐시 조회 실패 (Mission cache read failed)", e);
            return Optional.empty();
        }
    }

    public void put(MissionListResponse response) {
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
                            log.warn("미션 캐시 비동기 저장 실패 (Mission cache async write failed)", throwable);
                        }
                    });
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("미션 캐시 저장 실패 (Mission cache write failed)", e);
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
        if (version == null || version.isBlank()) {
            return properties.getKey();
        }
        return version + ":" + properties.getKey();
    }

    private MissionCachePayload toPayload(MissionListResponse response) {
        List<MissionCacheItem> missions = response.getMissions().stream()
                .map(item -> new MissionCacheItem(
                        item.getMissionId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getDifficulty(),
                        item.getMissionType(),
                        item.getCreatedAt()
                ))
                .toList();
        return new MissionCachePayload(missions);
    }

    private MissionListResponse fromPayload(MissionCachePayload payload) {
        if (payload == null || payload.getMissions() == null) {
            return MissionListResponse.of(List.of());
        }
        List<MissionResponse> missions = payload.getMissions().stream()
                .map(item -> MissionResponse.of(
                        item.getMissionId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getDifficulty(),
                        item.getMissionType(),
                        item.getCreatedAt()
                ))
                .toList();
        return MissionListResponse.of(missions);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MissionCachePayload {
        private List<MissionCacheItem> missions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MissionCacheItem {
        private Long missionId;
        private String title;
        private String description;
        private MissionDifficulty difficulty;
        private String missionType;
        private LocalDateTime createdAt;
    }
}
