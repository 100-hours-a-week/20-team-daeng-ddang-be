package com.daengddang.daengdong_map.service.cache;

import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingSummaryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
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
public class RankingPersonalCacheStore {

    private static final String NULL_REGION = "all";
    private static final String NULL_USER = "anon";

    private final RedisJsonBucketCacheHelper cacheHelper;
    private final CacheDefaultProperties defaultProperties;
    private final RankingPersonalCacheProperties properties;
    private final RankingPersonalCacheMetrics metrics;

    public Optional<PersonalRankingSummaryResponse> getSummary(String periodType,
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
                            PersonalRankingSummaryPayload.class,
                            metrics::recordMiss,
                            metrics::recordHit
                    )
                    .map(this::fromSummaryPayload);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("개인 랭킹 요약 캐시 조회 실패 (Personal ranking summary cache read failed)", e);
            return Optional.empty();
        }
    }

    public void putSummary(String periodType,
                           String periodValue,
                           Long regionId,
                           Long userId,
                           PersonalRankingSummaryResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            String key = buildSummaryKey(periodType, periodValue, regionId, userId);
            cacheHelper.writeAsync(
                    key,
                    toSummaryPayload(response),
                    resolveTtlSeconds(),
                    throwable -> {
                        metrics.recordWriteError();
                        log.warn("개인 랭킹 요약 캐시 저장 실패 (Personal ranking summary cache write failed)", throwable);
                    }
            );
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("개인 랭킹 요약 캐시 저장 실패 (Personal ranking summary cache write failed)", e);
        }
    }

    public Optional<PersonalRankingListResponse> getList(String periodType,
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
                            PersonalRankingListPayload.class,
                            metrics::recordMiss,
                            metrics::recordHit
                    )
                    .map(this::fromListPayload);
        } catch (Exception e) {
            metrics.recordReadError();
            metrics.recordFallbackError();
            log.warn("개인 랭킹 목록 캐시 조회 실패 (Personal ranking list cache read failed)", e);
            return Optional.empty();
        }
    }

    public void putList(String periodType,
                        String periodValue,
                        Long regionId,
                        String cursor,
                        int limit,
                        PersonalRankingListResponse response) {
        if (!isEnabled()) {
            return;
        }

        try {
            String key = buildListKey(periodType, periodValue, regionId, cursor, limit);
            cacheHelper.writeAsync(
                    key,
                    toListPayload(response),
                    resolveTtlSeconds(),
                    throwable -> {
                        metrics.recordWriteError();
                        log.warn("개인 랭킹 목록 캐시 저장 실패 (Personal ranking list cache write failed)", throwable);
                    }
            );
        } catch (Exception e) {
            metrics.recordWriteError();
            log.warn("개인 랭킹 목록 캐시 저장 실패 (Personal ranking list cache write failed)", e);
        }
    }

    public String buildSummaryKey(String periodType, String periodValue, Long regionId, Long userId) {
        return resolvePrefix(properties.getSummaryKey())
                + ":" + safe(periodType)
                + ":" + safe(periodValue)
                + ":" + resolveRegionPart(regionId)
                + ":" + resolveUserPart(userId);
    }

    public String buildListKey(String periodType, String periodValue, Long regionId, String cursor, int limit) {
        String rawCursor = cursor == null ? "" : cursor;
        String cursorHash = sha256Hex(rawCursor);
        return resolvePrefix(properties.getListKey())
                + ":" + safe(periodType)
                + ":" + safe(periodValue)
                + ":" + resolveRegionPart(regionId)
                + ":limit:" + limit
                + ":cursor:" + cursorHash;
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

    private String resolveRegionPart(Long regionId) {
        return regionId == null ? NULL_REGION : regionId.toString();
    }

    private String resolveUserPart(Long userId) {
        return userId == null ? NULL_USER : userId.toString();
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

    private PersonalRankingSummaryPayload toSummaryPayload(PersonalRankingSummaryResponse response) {
        return new PersonalRankingSummaryPayload(
                response.getTopRanks().stream().map(this::toItem).toList(),
                response.getMyRank() == null ? null : toItem(response.getMyRank())
        );
    }

    private PersonalRankingSummaryResponse fromSummaryPayload(PersonalRankingSummaryPayload payload) {
        List<PersonalRankItemResponse> topRanks = payload.getTopRanks() == null
                ? List.of()
                : payload.getTopRanks().stream().map(this::fromItem).toList();
        PersonalRankItemResponse myRank = payload.getMyRank() == null ? null : fromItem(payload.getMyRank());
        return PersonalRankingSummaryResponse.of(topRanks, myRank);
    }

    private PersonalRankingListPayload toListPayload(PersonalRankingListResponse response) {
        return new PersonalRankingListPayload(
                response.getRanks().stream().map(this::toItem).toList(),
                response.getNextCursor(),
                response.isHasNext()
        );
    }

    private PersonalRankingListResponse fromListPayload(PersonalRankingListPayload payload) {
        List<PersonalRankItemResponse> ranks = payload.getRanks() == null
                ? List.of()
                : payload.getRanks().stream().map(this::fromItem).toList();
        return PersonalRankingListResponse.of(ranks, payload.getNextCursor(), payload.isHasNext());
    }

    private PersonalRankItemPayload toItem(PersonalRankItemResponse item) {
        return new PersonalRankItemPayload(
                item.getRank(),
                item.getDogId(),
                item.getDogName(),
                item.getBirthDate(),
                item.getProfileImageUrl(),
                item.getBreed(),
                item.getTotalDistance()
        );
    }

    private PersonalRankItemResponse fromItem(PersonalRankItemPayload item) {
        return PersonalRankItemResponse.of(
                item.getRank(),
                item.getDogId(),
                item.getDogName(),
                item.getBirthDate(),
                item.getProfileImageUrl(),
                item.getBreed(),
                item.getTotalDistance()
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PersonalRankingSummaryPayload {
        private List<PersonalRankItemPayload> topRanks;
        private PersonalRankItemPayload myRank;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PersonalRankingListPayload {
        private List<PersonalRankItemPayload> ranks;
        private String nextCursor;
        private boolean hasNext;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PersonalRankItemPayload {
        private Integer rank;
        private Long dogId;
        private String dogName;
        private LocalDate birthDate;
        private String profileImageUrl;
        private String breed;
        private Double totalDistance;
    }
}
