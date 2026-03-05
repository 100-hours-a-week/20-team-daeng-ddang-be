package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.contribution.RegionContributionRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.RegionDogRankRepository;
import com.daengddang.daengdong_map.repository.projection.RegionContributionRankView;
import com.daengddang.daengdong_map.service.cache.RankingRegionContributionCacheMetrics;
import com.daengddang.daengdong_map.service.cache.RankingRegionContributionCacheStore;
import com.daengddang.daengdong_map.util.AccessValidator;
import com.daengddang.daengdong_map.util.RankingCursorCodec;
import com.daengddang.daengdong_map.util.RankingRequestValidator;
import com.daengddang.daengdong_map.util.RegionValidator;
import com.daengddang.daengdong_map.util.RankingValidator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionContributionRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final RegionDogRankRepository regionDogRankRepository;
    private final CursorPagingSupport cursorPagingSupport;
    private final RankingRequestValidator rankingRequestValidator;
    private final RankingCursorCodec rankingCursorCodec;
    private final RegionValidator regionValidator;
    private final AccessValidator accessValidator;
    private final RankingRegionContributionCacheStore rankingRegionContributionCacheStore;
    private final RankingRegionContributionCacheMetrics rankingRegionContributionCacheMetrics;
    private final ConcurrentHashMap<String, CompletableFuture<RegionContributionRankingSummaryResponse>> summaryInFlight =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<RegionContributionRankingListResponse>> listInFlight =
            new ConcurrentHashMap<>();

    public RegionContributionRankingSummaryResponse getRegionContributionRankingSummary(Long userId,
                                                                                        RankingPeriodRegionRequest dto) {
        rankingRequestValidator.validateRequestWithRegionId(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        Long regionId = dto.getRegionId();
        regionValidator.validateActiveRegion(regionId);

        Optional<RegionContributionRankingSummaryResponse> cached = rankingRegionContributionCacheStore.getSummary(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                userId
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingRegionContributionCacheStore.buildSummaryKey(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                userId
        );
        while (true) {
            CompletableFuture<RegionContributionRankingSummaryResponse> existing = summaryInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<RegionContributionRankingSummaryResponse> created = new CompletableFuture<>();
            if (summaryInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    RegionContributionRankingSummaryResponse response = loadSummaryFromDb(userId, periodType, dto.getPeriodValue(), regionId);
                    rankingRegionContributionCacheStore.putSummary(periodType.name(), dto.getPeriodValue(), regionId, userId, response);
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

    public RegionContributionRankingListResponse getRegionContributionRankingList(Long userId,
                                                                                  RankingPeriodRegionRequest dto,
                                                                                  RankingCursorRequest cursorDto) {
        rankingRequestValidator.validateRequestWithRegionId(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        Long regionId = dto.getRegionId();
        regionValidator.validateActiveRegion(regionId);

        String cursor = rankingRequestValidator.resolveCursor(cursorDto);
        int limit = rankingRequestValidator.resolveLimit(cursorDto);

        Optional<RegionContributionRankingListResponse> cached = rankingRegionContributionCacheStore.getList(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                cursor,
                limit
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingRegionContributionCacheStore.buildListKey(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                cursor,
                limit
        );
        while (true) {
            CompletableFuture<RegionContributionRankingListResponse> existing = listInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<RegionContributionRankingListResponse> created = new CompletableFuture<>();
            if (listInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    RegionContributionRankingListResponse response = loadListFromDb(periodType, dto.getPeriodValue(), regionId, cursor, limit);
                    rankingRegionContributionCacheStore.putList(periodType.name(), dto.getPeriodValue(), regionId, cursor, limit, response);
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

    private RegionContributionRankingSummaryResponse loadSummaryFromDb(Long userId,
                                                                       RankingPeriodType periodType,
                                                                       String periodValue,
                                                                       Long regionId) {
        rankingRegionContributionCacheMetrics.recordDbLoad();
        Dog dog = userId != null ? accessValidator.getDogOrThrow(userId) : null;

        List<RegionContributionRankItemResponse> topRanks = regionDogRankRepository
                .findRanks(periodType, periodValue, regionId, PageRequest.of(0, SUMMARY_TOP_LIMIT))
                .stream()
                .map(this::toContributionItem)
                .toList();

        RegionContributionRankItemResponse myRank = dog == null
                ? null
                : regionDogRankRepository
                .findMyRank(periodType, periodValue, regionId, dog.getId())
                .map(this::toContributionItem)
                .orElse(null);

        return RegionContributionRankingSummaryResponse.of(topRanks, myRank);
    }

    private RegionContributionRankingListResponse loadListFromDb(RankingPeriodType periodType,
                                                                 String periodValue,
                                                                 Long regionId,
                                                                 String cursor,
                                                                 int limit) {
        rankingRegionContributionCacheMetrics.recordDbLoad();
        CursorPagingSupport.CursorPageResult<RegionContributionRankItemResponse> page = cursorPagingSupport.paginate(
                cursor,
                limit,
                fetchSize -> regionDogRankRepository.findRanks(
                        periodType,
                        periodValue,
                        regionId,
                        PageRequest.of(0, fetchSize)
                ),
                RankingValidator::parseRankDogCursor,
                (parsedCursor, pageLimit) -> regionDogRankRepository.findRanksByCursor(
                        periodType,
                        periodValue,
                        regionId,
                        parsedCursor.rank(),
                        parsedCursor.dogId(),
                        PageRequest.of(0, pageLimit)
                ),
                this::toContributionItem,
                item -> rankingCursorCodec.toRankDogCursor(item.getRank(), item.getDogId())
        );

        return RegionContributionRankingListResponse.of(page.items(), page.nextCursor(), page.hasNext());
    }

    private RegionContributionRankItemResponse toContributionItem(RegionContributionRankView view) {
        return RegionContributionRankItemResponse.of(
                view.getRank(),
                view.getDogId(),
                view.getDogName(),
                view.getProfileImageUrl(),
                view.getDogDistance(),
                view.getContributionRate()
        );
    }
}
