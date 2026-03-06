package com.daengddang.daengdong_map.service.cache;

import com.daengddang.daengdong_map.dto.response.block.NearbyBlockListResponse;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockCacheStore {

    private final RedisJsonBucketCacheHelper cacheHelper;
    private final RedissonClient redissonClient;
    private final CacheDefaultProperties defaultProperties;
    private final BlockCacheProperties properties;
    private final BlockCachePolicy blockCachePolicy;
    private final BlockCacheKeyFactory blockCacheKeyFactory;
    private final BlockCacheMetrics metrics;

    public Optional<NearbyBlockListResponse> getNearby(int baseX, int baseY, int range) {
        if (!isEnabled()) {
            metrics.recordBypassDisabled();
            return Optional.empty();
        }

        try {
            return cacheHelper.read(
                            blockCacheKeyFactory.buildNearbyListKey(baseX, baseY, range),
                            BlockNearbyListPayload.class,
                            metrics::recordMiss,
                            metrics::recordHit
                    )
                    .map(this::fromPayload);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("블록 캐시 조회 실패", e);
            return Optional.empty();
        }
    }

    public void putNearby(int baseX, int baseY, int range, NearbyBlockListResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            cacheHelper.writeAsync(
                    blockCacheKeyFactory.buildNearbyListKey(baseX, baseY, range),
                    toPayload(response),
                    blockCachePolicy.resolveTtlSeconds(),
                    throwable -> {
                        metrics.recordWriteError();
                        log.warn("블록 캐시 저장 실패", throwable);
                    }
            );
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("블록 캐시 저장 실패", e);
        }
    }

    public long evictByChangedBlock(int changedBlockX, int changedBlockY) {
        if (!isEnabled()) {
            return 0L;
        }

        try {
            Set<String> keys = blockCachePolicy.buildInvalidateKeys(changedBlockX, changedBlockY);
            if (keys.isEmpty()) {
                return 0L;
            }
            return redissonClient.getKeys().delete(keys.toArray(String[]::new));
        } catch (Exception e) {
            log.warn("블록 캐시 무효화 실패", e);
            return 0L;
        }
    }

    private boolean isEnabled() {
        return properties.getEnabled() == null
                ? defaultProperties.isEnabled()
                : properties.getEnabled();
    }

    private BlockNearbyListPayload toPayload(NearbyBlockListResponse response) {
        List<BlockNearbyItemPayload> blocks = response.getBlocks().stream()
                .map(item -> new BlockNearbyItemPayload(
                        item.getBlockId(),
                        item.getDogId(),
                        item.getOccupiedAt()
                ))
                .toList();
        return new BlockNearbyListPayload(blocks);
    }

    private NearbyBlockListResponse fromPayload(BlockNearbyListPayload payload) {
        if (payload == null || payload.getBlocks() == null) {
            return NearbyBlockListResponse.from(List.of());
        }
        List<NearbyBlockResponse> blocks = payload.getBlocks().stream()
                .map(item -> NearbyBlockResponse.from(
                        item.getBlockId(),
                        item.getDogId(),
                        item.getOccupiedAt()
                ))
                .toList();
        return NearbyBlockListResponse.from(blocks);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class BlockNearbyListPayload {
        private List<BlockNearbyItemPayload> blocks;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class BlockNearbyItemPayload {
        private String blockId;
        private Long dogId;
        private LocalDateTime occupiedAt;
    }
}
