package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.region.RegionRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.RegionRepository;
import com.daengddang.daengdong_map.repository.RegionRankRepository;
import com.daengddang.daengdong_map.repository.projection.RegionRankView;
import com.daengddang.daengdong_map.service.cache.RankingRegionSummaryCacheMetrics;
import com.daengddang.daengdong_map.service.cache.RankingRegionSummaryCacheStore;
import com.daengddang.daengdong_map.service.ranking.zset.RankingZsetKeyFactory;
import com.daengddang.daengdong_map.service.ranking.zset.RankingZsetProperties;
import com.daengddang.daengdong_map.util.AccessValidator;
import com.daengddang.daengdong_map.util.RankingCursorCodec;
import com.daengddang.daengdong_map.util.RankingRequestValidator;
import com.daengddang.daengdong_map.util.RankingValidator;
import com.daengddang.daengdong_map.util.RegionValidator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final RegionRankRepository regionRankRepository;
    private final RegionRepository regionRepository;
    private final CursorPagingSupport cursorPagingSupport;
    private final RankingRequestValidator rankingRequestValidator;
    private final RankingCursorCodec rankingCursorCodec;
    private final RegionValidator regionValidator;
    private final AccessValidator accessValidator;
    private final RankingRegionSummaryCacheStore rankingRegionSummaryCacheStore;
    private final RankingRegionSummaryCacheMetrics rankingRegionSummaryCacheMetrics;
    private final RankingZsetProperties rankingZsetProperties;
    private final RankingZsetKeyFactory rankingZsetKeyFactory;
    private final RedissonClient redissonClient;
    private final ConcurrentHashMap<String, CompletableFuture<RegionRankingSummaryResponse>> summaryInFlight =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<RegionRankingListResponse>> listInFlight =
            new ConcurrentHashMap<>();

    public RegionRankingSummaryResponse getRegionRankingSummary(Long userId, RankingPeriodRegionRequest dto) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        Long requestedRegionId = dto.getRegionId();

        if (shouldReadFromZset(periodType)) {
            return getCachedOrLoadSummary(
                    periodType,
                    dto.getPeriodValue(),
                    requestedRegionId,
                    userId,
                    () -> loadSummaryFromZset(userId, periodType, dto.getPeriodValue(), requestedRegionId)
            );
        }

        return getCachedOrLoadSummary(
                periodType,
                dto.getPeriodValue(),
                requestedRegionId,
                userId,
                () -> loadSummaryFromDb(userId, periodType, dto.getPeriodValue(), requestedRegionId)
        );
    }

    private RegionRankingSummaryResponse getCachedOrLoadSummary(RankingPeriodType periodType,
                                                                String periodValue,
                                                                Long requestedRegionId,
                                                                Long userId,
                                                                Supplier<RegionRankingSummaryResponse> loader) {
        Optional<RegionRankingSummaryResponse> cached = rankingRegionSummaryCacheStore.getSummary(
                periodType.name(),
                periodValue,
                requestedRegionId,
                userId
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingRegionSummaryCacheStore.buildSummaryKey(
                periodType.name(),
                periodValue,
                requestedRegionId,
                userId
        );
        return loadSummaryWithSingleFlight(inflightKey, periodType, periodValue, requestedRegionId, userId, loader);
    }

    private RegionRankingSummaryResponse loadSummaryWithSingleFlight(String inflightKey,
                                                                     RankingPeriodType periodType,
                                                                     String periodValue,
                                                                     Long requestedRegionId,
                                                                     Long userId,
                                                                     Supplier<RegionRankingSummaryResponse> loader) {
        while (true) {
            CompletableFuture<RegionRankingSummaryResponse> existing = summaryInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<RegionRankingSummaryResponse> created = new CompletableFuture<>();
            if (summaryInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    RegionRankingSummaryResponse response = loader.get();
                    rankingRegionSummaryCacheStore.putSummary(
                            periodType.name(),
                            periodValue,
                            requestedRegionId,
                            userId,
                            response
                    );
                    created.complete(response);
                    return response;
                } catch (Exception e) {
                    created.completeExceptionally(e);
                    throw e;
                } finally {
                    summaryInFlight.remove(inflightKey, created);
                }
            }
        }
    }

    private RegionRankingSummaryResponse loadSummaryFromDb(Long userId,
                                                           RankingPeriodType periodType,
                                                           String periodValue,
                                                           Long requestedRegionId) {
        rankingRegionSummaryCacheMetrics.recordDbLoad();
        User user = userId != null ? accessValidator.getUserOrThrow(userId) : null;

        Long regionId = resolveRegionId(user, requestedRegionId);

        List<RegionRankItemResponse> topRanks = regionRankRepository
                .findRanks(periodType, periodValue, PageRequest.of(0, SUMMARY_TOP_LIMIT))
                .stream()
                .map(this::toRegionRankItem)
                .toList();

        RegionRankItemResponse myRank = regionId == null
                ? null
                : regionRankRepository
                .findMyRegionRank(periodType, periodValue, regionId)
                .map(this::toRegionRankItem)
                .orElse(null);

        return RegionRankingSummaryResponse.of(topRanks, myRank);
    }

    private RegionRankingSummaryResponse loadSummaryFromZset(Long userId,
                                                             RankingPeriodType periodType,
                                                             String periodValue,
                                                             Long requestedRegionId) {
        try {
            User user = userId != null ? accessValidator.getUserOrThrow(userId) : null;
            Long myRegionId = resolveRegionId(user, requestedRegionId);

            String key = rankingZsetKeyFactory.regionKey(periodType, periodValue);
            RBatch batch = redissonClient.createBatch();
            RScoredSortedSetAsync<String> zsetAsync = batch.getScoredSortedSet(key, StringCodec.INSTANCE);
            RFuture<Collection<ScoredEntry<String>>> topEntriesFuture =
                    zsetAsync.entryRangeReversedAsync(0, SUMMARY_TOP_LIMIT - 1);
            RFuture<Integer> revRankFuture = null;
            RFuture<Double> scoreFuture = null;
            if (myRegionId != null) {
                String myRegionIdString = String.valueOf(myRegionId);
                revRankFuture = zsetAsync.revRankAsync(myRegionIdString);
                scoreFuture = zsetAsync.getScoreAsync(myRegionIdString);
            }
            batch.execute();

            Collection<ScoredEntry<String>> fetchedTopEntries = topEntriesFuture.getNow();
            List<ScoredEntry<String>> topEntries = fetchedTopEntries == null
                    ? new ArrayList<>()
                    : new ArrayList<>(fetchedTopEntries);
            Integer revRank = revRankFuture == null ? null : revRankFuture.getNow();
            Double myScore = scoreFuture == null ? null : scoreFuture.getNow();

            List<Long> parsedRegionIds = parseRegionIds(topEntries, topEntries.size());
            Map<Long, String> regionNameById = getRegionNameById(parsedRegionIds, myRegionId);
            List<RegionRankItemResponse> topRanks =
                    buildRankItemsFromEntries(topEntries, parsedRegionIds, regionNameById, 1, topEntries.size());

            RegionRankItemResponse myRank = null;
            if (myRegionId != null && revRank != null) {
                String regionName = regionNameById.get(myRegionId);
                if (regionName != null) {
                    Double score = findScoreInEntries(topEntries, myRegionId);
                    if (score == null) {
                        score = myScore;
                    }
                    myRank = RegionRankItemResponse.of(
                            revRank + 1,
                            myRegionId,
                            regionName,
                            score == null ? 0.0 : score
                    );
                }
            }
            return RegionRankingSummaryResponse.of(topRanks, myRank);
        } catch (Exception e) {
            return loadSummaryFromDb(userId, periodType, periodValue, requestedRegionId);
        }
    }

    public RegionRankingListResponse getRegionRankingList(Long userId,
                                                          RankingPeriodRequest dto,
                                                          RankingCursorRequest cursorDto) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());

        String cursor = rankingRequestValidator.resolveCursor(cursorDto);
        int limit = rankingRequestValidator.resolveLimit(cursorDto);
        if (shouldReadFromZset(periodType)) {
            if (isListCacheTarget(cursor)) {
                return getCachedOrLoadList(
                        periodType,
                        dto.getPeriodValue(),
                        cursor,
                        limit,
                        () -> loadListFromZset(periodType, dto.getPeriodValue(), cursor, limit)
                );
            }
            return loadListFromZset(periodType, dto.getPeriodValue(), cursor, limit);
        }

        if (isListCacheTarget(cursor)) {
            return getCachedOrLoadList(
                    periodType,
                    dto.getPeriodValue(),
                    cursor,
                    limit,
                    () -> loadListFromDb(periodType, dto.getPeriodValue(), cursor, limit)
            );
        }

        return loadListFromDb(periodType, dto.getPeriodValue(), cursor, limit);
    }

    private RegionRankingListResponse getCachedOrLoadList(RankingPeriodType periodType,
                                                          String periodValue,
                                                          String cursor,
                                                          int limit,
                                                          Supplier<RegionRankingListResponse> loader) {
        Optional<RegionRankingListResponse> cached = rankingRegionSummaryCacheStore.getList(
                periodType.name(),
                periodValue,
                cursor,
                limit
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingRegionSummaryCacheStore.buildListKey(
                periodType.name(),
                periodValue,
                cursor,
                limit
        );
        return loadListWithSingleFlight(inflightKey, periodType, periodValue, cursor, limit, loader);
    }

    private RegionRankingListResponse loadListWithSingleFlight(String inflightKey,
                                                               RankingPeriodType periodType,
                                                               String periodValue,
                                                               String cursor,
                                                               int limit,
                                                               Supplier<RegionRankingListResponse> loader) {
        while (true) {
            CompletableFuture<RegionRankingListResponse> existing = listInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<RegionRankingListResponse> created = new CompletableFuture<>();
            if (listInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    RegionRankingListResponse response = loader.get();
                    rankingRegionSummaryCacheStore.putList(periodType.name(), periodValue, cursor, limit, response);
                    created.complete(response);
                    return response;
                } catch (Exception e) {
                    created.completeExceptionally(e);
                    throw e;
                } finally {
                    listInFlight.remove(inflightKey, created);
                }
            }
        }
    }

    private RegionRankingListResponse loadListFromDb(RankingPeriodType periodType,
                                                     String periodValue,
                                                     String cursor,
                                                     int limit) {
        rankingRegionSummaryCacheMetrics.recordDbLoad();
        CursorPagingSupport.CursorPageResult<RegionRankItemResponse> page = cursorPagingSupport.paginate(
                cursor,
                limit,
                fetchSize -> regionRankRepository.findRanks(periodType, periodValue, PageRequest.of(0, fetchSize)),
                RankingValidator::parseRankRegionCursor,
                (parsedCursor, pageLimit) -> regionRankRepository.findRanksByCursor(
                        periodType,
                        periodValue,
                        parsedCursor.rank(),
                        parsedCursor.regionId(),
                        PageRequest.of(0, pageLimit)
                ),
                this::toRegionRankItem,
                item -> rankingCursorCodec.toRankRegionCursor(item.getRank(), item.getRegionId())
        );

        return RegionRankingListResponse.of(page.items(), page.nextCursor(), page.hasNext());
    }

    private RegionRankingListResponse loadListFromZset(RankingPeriodType periodType,
                                                       String periodValue,
                                                       String cursor,
                                                       int limit) {
        try {
            String key = rankingZsetKeyFactory.regionKey(periodType, periodValue);
            RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key, StringCodec.INSTANCE);

            int startIndex = 0;
            if (cursor != null && !cursor.isBlank()) {
                RankingValidator.RankRegionCursor parsed = RankingValidator.parseRankRegionCursor(cursor);
                startIndex = Math.max(parsed.rank(), 0);
            }

            int endIndexWithExtra = Math.max(startIndex + limit, startIndex);
            List<ScoredEntry<String>> entries = new ArrayList<>(zset.entryRangeReversed(startIndex, endIndexWithExtra));
            boolean hasNext = entries.size() > limit;
            int realSize = Math.min(entries.size(), limit);

            List<Long> parsedRegionIds = parseRegionIds(entries, realSize);
            Map<Long, String> regionNameById = getRegionNameById(parsedRegionIds, null);
            List<RegionRankItemResponse> ranks =
                    buildRankItemsFromEntries(entries, parsedRegionIds, regionNameById, startIndex + 1, realSize);

            String nextCursor = null;
            if (hasNext && !ranks.isEmpty()) {
                RegionRankItemResponse last = ranks.get(ranks.size() - 1);
                nextCursor = rankingCursorCodec.toRankRegionCursor(last.getRank(), last.getRegionId());
            }
            return RegionRankingListResponse.of(ranks, nextCursor, hasNext);
        } catch (Exception e) {
            return loadListFromDb(periodType, periodValue, cursor, limit);
        }
    }

    private Long resolveRegionId(User user, Long requestedRegionId) {
        if (requestedRegionId != null) {
            regionValidator.validateActiveRegion(requestedRegionId);
            return requestedRegionId;
        }
        if (user == null) {
            return null;
        }

        Region userRegion = user.getRegion();
        if (userRegion == null) {
            throw new BaseException(ErrorCode.REGION_NOT_FOUND);
        }

        regionValidator.validateActiveRegion(userRegion.getId());
        return userRegion.getId();
    }

    private RegionRankItemResponse toRegionRankItem(RegionRankView view) {
        return RegionRankItemResponse.of(
                view.getRank(),
                view.getRegionId(),
                view.getRegionName(),
                view.getTotalDistance()
        );
    }

    private boolean shouldReadFromZset(RankingPeriodType periodType) {
        return rankingZsetProperties.isEnabled()
                && periodType == RankingPeriodType.WEEK
                && "zset".equalsIgnoreCase(rankingZsetProperties.getReadSource());
    }

    private boolean isListCacheTarget(String cursor) {
        return cursor == null || cursor.isBlank();
    }

    private List<Long> parseRegionIds(List<ScoredEntry<String>> entries, int sizeLimit) {
        int realSize = Math.min(entries.size(), sizeLimit);
        List<Long> regionIds = new ArrayList<>(realSize);
        for (int i = 0; i < realSize; i++) {
            regionIds.add(toLongOrNull(entries.get(i).getValue()));
        }
        return regionIds;
    }

    private Map<Long, String> getRegionNameById(List<Long> parsedRegionIds, Long additionalRegionId) {
        java.util.LinkedHashSet<Long> regionIds = new java.util.LinkedHashSet<>();
        for (Long regionId : parsedRegionIds) {
            if (regionId != null) {
                regionIds.add(regionId);
            }
        }
        if (additionalRegionId != null) {
            regionIds.add(additionalRegionId);
        }
        return regionRepository.findAllById(regionIds).stream()
                .collect(java.util.stream.Collectors.toMap(Region::getId, Region::getName));
    }

    private List<RegionRankItemResponse> buildRankItemsFromEntries(List<ScoredEntry<String>> entries,
                                                                   List<Long> parsedRegionIds,
                                                                   Map<Long, String> regionNameById,
                                                                   int baseRank,
                                                                   int sizeLimit) {
        int realSize = Math.min(entries.size(), sizeLimit);
        List<RegionRankItemResponse> items = new ArrayList<>(realSize);
        for (int i = 0; i < realSize; i++) {
            RegionRankItemResponse item = toRankItem(entries.get(i), parsedRegionIds.get(i), regionNameById, baseRank + i);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private RegionRankItemResponse toRankItem(ScoredEntry<String> entry,
                                              Long regionId,
                                              Map<Long, String> regionNameById,
                                              int rank) {
        if (regionId == null) {
            return null;
        }
        String regionName = regionNameById.get(regionId);
        if (regionName == null) {
            return null;
        }
        return RegionRankItemResponse.of(
                rank,
                regionId,
                regionName,
                entry.getScore() == null ? 0.0 : entry.getScore()
        );
    }

    private Double findScoreInEntries(List<ScoredEntry<String>> entries, Long regionId) {
        String regionIdString = String.valueOf(regionId);
        for (ScoredEntry<String> entry : entries) {
            if (regionIdString.equals(entry.getValue())) {
                return entry.getScore();
            }
        }
        return null;
    }

    private Long toLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}
