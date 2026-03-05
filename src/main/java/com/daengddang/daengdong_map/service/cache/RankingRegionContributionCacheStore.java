package com.daengddang.daengdong_map.service.cache;

import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingSummaryResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingRegionContributionCacheStore {

    private static final String NULL_USER = "anon";

    private final RedisJsonBucketCacheHelper cacheHelper;
    private final CacheDefaultProperties defaultProperties;
    private final RankingRegionContributionCacheProperties properties;
    private final RankingRegionContributionCacheMetrics metrics;

    public Optional<RegionContributionRankingSummaryResponse> getSummary(String periodType,
                                                                         String periodValue,
                                                                         Long regionId,
                                                                         Long userId) {
        if (!isEnabled()) {
            metrics.recordBypassDisabled();
            return Optional.empty();
        }

        try {
            return cacheHelper.read(
                            buildSummaryKey(periodType, periodValue, regionId, userId),
                            RegionContributionSummaryPayload.class,
                            metrics::recordMiss,
                            metrics::recordHit
                    )
                    .map(this::fromSummaryPayload);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("기여도 랭킹 요약 캐시 조회 실패 (Region contribution summary cache read failed)", e);
            return Optional.empty();
        }
    }

    public void putSummary(String periodType,
                           String periodValue,
                           Long regionId,
                           Long userId,
                           RegionContributionRankingSummaryResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            cacheHelper.writeAsync(
                    buildSummaryKey(periodType, periodValue, regionId, userId),
                    toSummaryPayload(response),
                    resolveTtlSeconds(),
                    throwable -> {
                        metrics.recordWriteError();
                        log.warn("기여도 랭킹 요약 캐시 저장 실패 (Region contribution summary cache write failed)", throwable);
                    }
            );
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("기여도 랭킹 요약 캐시 저장 실패 (Region contribution summary cache write failed)", e);
        }
    }

    public Optional<RegionContributionRankingListResponse> getList(String periodType,
                                                                   String periodValue,
                                                                   Long regionId,
                                                                   String cursor,
                                                                   int limit) {
        if (!isEnabled()) {
            metrics.recordBypassDisabled();
            return Optional.empty();
        }

        try {
            return cacheHelper.read(
                            buildListKey(periodType, periodValue, regionId, cursor, limit),
                            RegionContributionListPayload.class,
                            metrics::recordMiss,
                            metrics::recordHit
                    )
                    .map(this::fromListPayload);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("기여도 랭킹 목록 캐시 조회 실패 (Region contribution list cache read failed)", e);
            return Optional.empty();
        }
    }

    public void putList(String periodType,
                        String periodValue,
                        Long regionId,
                        String cursor,
                        int limit,
                        RegionContributionRankingListResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            cacheHelper.writeAsync(
                    buildListKey(periodType, periodValue, regionId, cursor, limit),
                    toListPayload(response),
                    resolveTtlSeconds(),
                    throwable -> {
                        metrics.recordWriteError();
                        log.warn("기여도 랭킹 목록 캐시 저장 실패 (Region contribution list cache write failed)", throwable);
                    }
            );
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("기여도 랭킹 목록 캐시 저장 실패 (Region contribution list cache write failed)", e);
        }
    }

    public String buildSummaryKey(String periodType, String periodValue, Long regionId, Long userId) {
        return resolvePrefix(properties.getSummaryKey())
                + ":" + safe(periodType)
                + ":" + safe(periodValue)
                + ":" + regionId
                + ":" + (userId == null ? NULL_USER : userId);
    }

    public String buildListKey(String periodType, String periodValue, Long regionId, String cursor, int limit) {
        String rawCursor = cursor == null ? "" : cursor;
        return resolvePrefix(properties.getListKey())
                + ":" + safe(periodType)
                + ":" + safe(periodValue)
                + ":" + regionId
                + ":limit:" + limit
                + ":cursor:" + sha256Hex(rawCursor);
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

    private RegionContributionSummaryPayload toSummaryPayload(RegionContributionRankingSummaryResponse response) {
        return new RegionContributionSummaryPayload(
                response.getTopRanks().stream().map(this::toItem).toList(),
                response.getMyRank() == null ? null : toItem(response.getMyRank())
        );
    }

    private RegionContributionRankingSummaryResponse fromSummaryPayload(RegionContributionSummaryPayload payload) {
        List<RegionContributionRankItemResponse> topRanks = payload.getTopRanks() == null
                ? List.of()
                : payload.getTopRanks().stream().map(this::fromItem).toList();
        RegionContributionRankItemResponse myRank = payload.getMyRank() == null ? null : fromItem(payload.getMyRank());
        return RegionContributionRankingSummaryResponse.of(topRanks, myRank);
    }

    private RegionContributionListPayload toListPayload(RegionContributionRankingListResponse response) {
        return new RegionContributionListPayload(
                response.getRanks().stream().map(this::toItem).toList(),
                response.getNextCursor(),
                response.isHasNext()
        );
    }

    private RegionContributionRankingListResponse fromListPayload(RegionContributionListPayload payload) {
        List<RegionContributionRankItemResponse> ranks = payload.getRanks() == null
                ? List.of()
                : payload.getRanks().stream().map(this::fromItem).toList();
        return RegionContributionRankingListResponse.of(ranks, payload.getNextCursor(), payload.isHasNext());
    }

    private RegionContributionRankItemPayload toItem(RegionContributionRankItemResponse item) {
        return new RegionContributionRankItemPayload(
                item.getRank(),
                item.getDogId(),
                item.getDogName(),
                item.getProfileImageUrl(),
                item.getDogDistance(),
                item.getContributionRate()
        );
    }

    private RegionContributionRankItemResponse fromItem(RegionContributionRankItemPayload item) {
        return RegionContributionRankItemResponse.of(
                item.getRank(),
                item.getDogId(),
                item.getDogName(),
                item.getProfileImageUrl(),
                item.getDogDistance(),
                item.getContributionRate()
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RegionContributionSummaryPayload {
        private List<RegionContributionRankItemPayload> topRanks;
        private RegionContributionRankItemPayload myRank;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RegionContributionListPayload {
        private List<RegionContributionRankItemPayload> ranks;
        private String nextCursor;
        private boolean hasNext;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RegionContributionRankItemPayload {
        private Integer rank;
        private Long dogId;
        private String dogName;
        private String profileImageUrl;
        private Double dogDistance;
        private Double contributionRate;
    }
}
