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
import com.daengddang.daengdong_map.repository.RegionRankRepository;
import com.daengddang.daengdong_map.repository.projection.RegionRankView;
import com.daengddang.daengdong_map.service.cache.RankingRegionSummaryCacheMetrics;
import com.daengddang.daengdong_map.service.cache.RankingRegionSummaryCacheStore;
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
public class RegionRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final RegionRankRepository regionRankRepository;
    private final CursorPagingSupport cursorPagingSupport;
    private final RankingRequestValidator rankingRequestValidator;
    private final RankingCursorCodec rankingCursorCodec;
    private final RegionValidator regionValidator;
    private final AccessValidator accessValidator;
    private final RankingRegionSummaryCacheStore rankingRegionSummaryCacheStore;
    private final RankingRegionSummaryCacheMetrics rankingRegionSummaryCacheMetrics;
    private final ConcurrentHashMap<String, CompletableFuture<RegionRankingSummaryResponse>> summaryInFlight =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<RegionRankingListResponse>> listInFlight =
            new ConcurrentHashMap<>();

    public RegionRankingSummaryResponse getRegionRankingSummary(Long userId, RankingPeriodRegionRequest dto) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        Long requestedRegionId = dto.getRegionId();

        Optional<RegionRankingSummaryResponse> cached = rankingRegionSummaryCacheStore.getSummary(
                periodType.name(),
                dto.getPeriodValue(),
                requestedRegionId,
                userId
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingRegionSummaryCacheStore.buildSummaryKey(
                periodType.name(),
                dto.getPeriodValue(),
                requestedRegionId,
                userId
        );
        while (true) {
            CompletableFuture<RegionRankingSummaryResponse> existing = summaryInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<RegionRankingSummaryResponse> created = new CompletableFuture<>();
            if (summaryInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    RegionRankingSummaryResponse response = loadSummaryFromDb(userId, periodType, dto.getPeriodValue(), requestedRegionId);
                    rankingRegionSummaryCacheStore.putSummary(
                            periodType.name(),
                            dto.getPeriodValue(),
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

    public RegionRankingListResponse getRegionRankingList(Long userId,
                                                          RankingPeriodRequest dto,
                                                          RankingCursorRequest cursorDto) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());

        String cursor = rankingRequestValidator.resolveCursor(cursorDto);
        int limit = rankingRequestValidator.resolveLimit(cursorDto);

        Optional<RegionRankingListResponse> cached = rankingRegionSummaryCacheStore.getList(
                periodType.name(),
                dto.getPeriodValue(),
                cursor,
                limit
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingRegionSummaryCacheStore.buildListKey(
                periodType.name(),
                dto.getPeriodValue(),
                cursor,
                limit
        );
        while (true) {
            CompletableFuture<RegionRankingListResponse> existing = listInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<RegionRankingListResponse> created = new CompletableFuture<>();
            if (listInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    RegionRankingListResponse response = loadListFromDb(periodType, dto.getPeriodValue(), cursor, limit);
                    rankingRegionSummaryCacheStore.putList(periodType.name(), dto.getPeriodValue(), cursor, limit, response);
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
}
