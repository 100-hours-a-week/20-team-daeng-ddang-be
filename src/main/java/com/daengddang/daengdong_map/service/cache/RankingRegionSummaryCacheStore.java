package com.daengddang.daengdong_map.service.cache;

import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingSummaryResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
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
public class RankingRegionSummaryCacheStore {

    private static final String NULL_REGION = "all";
    private static final String NULL_USER = "anon";

    private final RedissonClient redissonClient;
    private final RedisJsonBucketCacheHelper cacheHelper;
    private final CacheDefaultProperties defaultProperties;
    private final RankingRegionSummaryCacheProperties properties;
    private final RankingRegionSummaryCacheMetrics metrics;

    public Optional<RegionRankingSummaryResponse> getSummary(String periodType,
                                                             String periodValue,
                                                             Long requestedRegionId,
                                                             Long userId) {
        if (!isEnabled()) {
            metrics.recordBypassDisabled();
            return Optional.empty();
        }

        try {
            return cacheHelper.read(
                            buildSummaryKey(periodType, periodValue, requestedRegionId, userId),
                            RegionRankingSummaryPayload.class,
                            metrics::recordMiss,
                            metrics::recordHit
                    )
                    .map(this::fromPayload);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("지역 랭킹 요약 캐시 조회 실패 (Region ranking summary cache read failed)", e);
            return Optional.empty();
        }
    }

    public void putSummary(String periodType,
                           String periodValue,
                           Long requestedRegionId,
                           Long userId,
                           RegionRankingSummaryResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            cacheHelper.writeAsync(
                    buildSummaryKey(periodType, periodValue, requestedRegionId, userId),
                    toPayload(response),
                    resolveTtlSeconds(),
                    throwable -> {
                        metrics.recordWriteError();
                        log.warn("지역 랭킹 요약 캐시 저장 실패 (Region ranking summary cache write failed)", throwable);
                    }
            );
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("지역 랭킹 요약 캐시 저장 실패 (Region ranking summary cache write failed)", e);
        }
    }

    public String buildSummaryKey(String periodType, String periodValue, Long requestedRegionId, Long userId) {
        return resolvePrefix(properties.getSummaryKey())
                + ":" + safe(periodType)
                + ":" + safe(periodValue)
                + ":" + (requestedRegionId == null ? NULL_REGION : requestedRegionId)
                + ":" + (userId == null ? NULL_USER : userId);
    }

    public Optional<RegionRankingListResponse> getList(String periodType,
                                                       String periodValue,
                                                       String cursor,
                                                       int limit) {
        if (!isEnabled()) {
            metrics.recordBypassDisabled();
            return Optional.empty();
        }

        try {
            return cacheHelper.read(
                            buildListKey(periodType, periodValue, cursor, limit),
                            RegionRankingListPayload.class,
                            metrics::recordMiss,
                            metrics::recordHit
                    )
                    .map(this::fromListPayload);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("지역 랭킹 목록 캐시 조회 실패 (Region ranking list cache read failed)", e);
            return Optional.empty();
        }
    }

    public void putList(String periodType,
                        String periodValue,
                        String cursor,
                        int limit,
                        RegionRankingListResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            cacheHelper.writeAsync(
                    buildListKey(periodType, periodValue, cursor, limit),
                    toListPayload(response),
                    resolveTtlSeconds(),
                    throwable -> {
                        metrics.recordWriteError();
                        log.warn("지역 랭킹 목록 캐시 저장 실패 (Region ranking list cache write failed)", throwable);
                    }
            );
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("지역 랭킹 목록 캐시 저장 실패 (Region ranking list cache write failed)", e);
        }
    }

    public String buildListKey(String periodType, String periodValue, String cursor, int limit) {
        String rawCursor = cursor == null ? "" : cursor;
        return resolvePrefix(properties.getListKey())
                + ":" + safe(periodType)
                + ":" + safe(periodValue)
                + ":limit:" + limit
                + ":cursor:" + sha256Hex(rawCursor);
    }

    public long evictAll() {
        if (!isEnabled()) {
            return 0L;
        }

        try {
            String summaryPattern = resolvePrefix(properties.getSummaryKey()) + ":*";
            String listPattern = resolvePrefix(properties.getListKey()) + ":*";
            List<String> keys = new ArrayList<>();
            redissonClient.getKeys().getKeysByPattern(summaryPattern).forEach(keys::add);
            redissonClient.getKeys().getKeysByPattern(listPattern).forEach(keys::add);
            if (keys.isEmpty()) {
                return 0L;
            }
            return redissonClient.getKeys().delete(keys.toArray(String[]::new));
        } catch (Exception e) {
            log.warn("지역 랭킹 캐시 무효화 실패 (Region ranking cache eviction failed)", e);
            return 0L;
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

    private String resolvePrefix(String baseKey) {
        String version = properties.getKeyVersion() == null
                ? defaultProperties.getKeyVersion()
                : properties.getKeyVersion();
        if (version == null || version.isBlank()) {
            return baseKey;
        }
        return version + ":" + baseKey;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "none" : value;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private RegionRankingSummaryPayload toPayload(RegionRankingSummaryResponse response) {
        return new RegionRankingSummaryPayload(
                response.getTopRanks().stream().map(this::toItem).toList(),
                response.getMyRank() == null ? null : toItem(response.getMyRank())
        );
    }

    private RegionRankingSummaryResponse fromPayload(RegionRankingSummaryPayload payload) {
        List<RegionRankItemResponse> topRanks = payload.getTopRanks() == null
                ? List.of()
                : payload.getTopRanks().stream().map(this::fromItem).toList();
        RegionRankItemResponse myRank = payload.getMyRank() == null ? null : fromItem(payload.getMyRank());
        return RegionRankingSummaryResponse.of(topRanks, myRank);
    }

    private RegionRankItemPayload toItem(RegionRankItemResponse item) {
        return new RegionRankItemPayload(
                item.getRank(),
                item.getRegionId(),
                item.getRegionName(),
                item.getTotalDistance()
        );
    }

    private RegionRankingListPayload toListPayload(RegionRankingListResponse response) {
        return new RegionRankingListPayload(
                response.getRanks().stream().map(this::toItem).toList(),
                response.getNextCursor(),
                response.isHasNext()
        );
    }

    private RegionRankItemResponse fromItem(RegionRankItemPayload item) {
        return RegionRankItemResponse.of(
                item.getRank(),
                item.getRegionId(),
                item.getRegionName(),
                item.getTotalDistance()
        );
    }

    private RegionRankingListResponse fromListPayload(RegionRankingListPayload payload) {
        List<RegionRankItemResponse> ranks = payload.getRanks() == null
                ? List.of()
                : payload.getRanks().stream().map(this::fromItem).toList();
        return RegionRankingListResponse.of(ranks, payload.getNextCursor(), payload.isHasNext());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RegionRankingSummaryPayload {
        private List<RegionRankItemPayload> topRanks;
        private RegionRankItemPayload myRank;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RegionRankingListPayload {
        private List<RegionRankItemPayload> ranks;
        private String nextCursor;
        private boolean hasNext;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RegionRankItemPayload {
        private Integer rank;
        private Long regionId;
        private String regionName;
        private Double totalDistance;
    }
}
