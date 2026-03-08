package com.daengddang.daengdong_map.service.ranking;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingListResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankItemResponse;
import com.daengddang.daengdong_map.dto.response.ranking.personal.PersonalRankingSummaryResponse;
import com.daengddang.daengdong_map.repository.DogGlobalRankRepository;
import com.daengddang.daengdong_map.repository.DogRankRepository;
import com.daengddang.daengdong_map.repository.projection.DogRankView;
import com.daengddang.daengdong_map.service.cache.RankingPersonalCacheMetrics;
import com.daengddang.daengdong_map.service.cache.RankingPersonalCacheStore;
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
public class PersonalRankingService {

    private static final int SUMMARY_TOP_LIMIT = 3;

    private final DogGlobalRankRepository dogGlobalRankRepository;
    private final DogRankRepository dogRankRepository;
    private final CursorPagingSupport cursorPagingSupport;
    private final RankingRequestValidator rankingRequestValidator;
    private final RankingCursorCodec rankingCursorCodec;
    private final RegionValidator regionValidator;
    private final AccessValidator accessValidator;
    private final RankingPersonalCacheStore rankingPersonalCacheStore;
    private final RankingPersonalCacheMetrics rankingPersonalCacheMetrics;
    private final ConcurrentHashMap<String, CompletableFuture<PersonalRankingSummaryResponse>> summaryInFlight =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<PersonalRankingListResponse>> listInFlight =
            new ConcurrentHashMap<>();

    public PersonalRankingSummaryResponse getPersonalRankingSummary(Long userId, RankingPeriodRegionRequest dto) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());
        Long regionId = dto.getRegionId();

        Optional<PersonalRankingSummaryResponse> cached = rankingPersonalCacheStore.getSummary(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                userId
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingPersonalCacheStore.buildSummaryKey(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                userId
        );
        return loadSummaryWithSingleFlight(inflightKey, userId, periodType, dto.getPeriodValue(), regionId);
    }

    private PersonalRankingSummaryResponse loadSummaryWithSingleFlight(String inflightKey,
                                                                       Long userId,
                                                                       RankingPeriodType periodType,
                                                                       String periodValue,
                                                                       Long regionId) {
        while (true) {
            CompletableFuture<PersonalRankingSummaryResponse> existing = summaryInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<PersonalRankingSummaryResponse> created = new CompletableFuture<>();
            if (summaryInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    PersonalRankingSummaryResponse response = loadSummaryFromDb(userId, periodType, periodValue, regionId);
                    rankingPersonalCacheStore.putSummary(periodType.name(), periodValue, regionId, userId, response);
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

    private PersonalRankingSummaryResponse loadSummaryFromDb(Long userId,
                                                             RankingPeriodType periodType,
                                                             String periodValue,
                                                             Long regionId) {
        rankingPersonalCacheMetrics.recordDbLoad();
        Dog dog = userId != null ? accessValidator.getDogOrThrow(userId) : null;

        List<PersonalRankItemResponse> topRanks;
        PersonalRankItemResponse myRank;
        if (regionId == null) {
            topRanks = dogGlobalRankRepository
                    .findRanks(periodType, periodValue, PageRequest.of(0, SUMMARY_TOP_LIMIT))
                    .stream()
                    .map(this::toPersonalRankItem)
                    .toList();

            myRank = dog == null
                    ? null
                    : dogGlobalRankRepository
                    .findMyRank(periodType, periodValue, dog.getId())
                    .map(this::toPersonalRankItem)
                    .orElse(null);
        } else {
            regionValidator.validateActiveRegion(regionId);
            topRanks = dogRankRepository
                    .findRanks(periodType, periodValue, regionId, PageRequest.of(0, SUMMARY_TOP_LIMIT))
                    .stream()
                    .map(this::toPersonalRankItem)
                    .toList();

            myRank = dog == null
                    ? null
                    : dogRankRepository
                    .findMyRank(periodType, periodValue, regionId, dog.getId())
                    .map(this::toPersonalRankItem)
                    .orElse(null);
        }

        return PersonalRankingSummaryResponse.of(topRanks, myRank);
    }

    public PersonalRankingListResponse getPersonalRankingList(Long userId,
                                                              RankingPeriodRegionRequest dto,
                                                              RankingCursorRequest cursorRequest) {
        rankingRequestValidator.validateRequestNotNull(dto);
        RankingPeriodType periodType = rankingRequestValidator.parseAndValidatePeriod(dto.getPeriodType(), dto.getPeriodValue());

        Long regionId = dto.getRegionId();
        if (regionId != null) {
            regionValidator.validateActiveRegion(regionId);
        }
        String cursor = rankingRequestValidator.resolveCursor(cursorRequest);
        int limit = rankingRequestValidator.resolveLimit(cursorRequest);

        Optional<PersonalRankingListResponse> cached = rankingPersonalCacheStore.getList(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                cursor,
                limit
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        String inflightKey = rankingPersonalCacheStore.buildListKey(
                periodType.name(),
                dto.getPeriodValue(),
                regionId,
                cursor,
                limit
        );
        return loadListWithSingleFlight(inflightKey, periodType, dto.getPeriodValue(), regionId, cursor, limit);
    }

    private PersonalRankingListResponse loadListWithSingleFlight(String inflightKey,
                                                                 RankingPeriodType periodType,
                                                                 String periodValue,
                                                                 Long regionId,
                                                                 String cursor,
                                                                 int limit) {
        while (true) {
            CompletableFuture<PersonalRankingListResponse> existing = listInFlight.get(inflightKey);
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<PersonalRankingListResponse> created = new CompletableFuture<>();
            if (listInFlight.putIfAbsent(inflightKey, created) == null) {
                try {
                    PersonalRankingListResponse response = loadListFromDb(periodType, periodValue, regionId, cursor, limit);
                    rankingPersonalCacheStore.putList(periodType.name(), periodValue, regionId, cursor, limit, response);
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

    private PersonalRankingListResponse loadListFromDb(RankingPeriodType periodType,
                                                       String periodValue,
                                                       Long regionId,
                                                       String cursor,
                                                       int limit) {
        rankingPersonalCacheMetrics.recordDbLoad();
        CursorPagingSupport.CursorPageResult<PersonalRankItemResponse> page = cursorPagingSupport.paginate(
                cursor,
                limit,
                fetchSize -> regionId == null
                        ? dogGlobalRankRepository.findRanks(periodType, periodValue, PageRequest.of(0, fetchSize))
                        : dogRankRepository.findRanks(periodType, periodValue, regionId, PageRequest.of(0, fetchSize)),
                RankingValidator::parseRankDogCursor,
                (parsedCursor, pageLimit) -> regionId == null
                        ? dogGlobalRankRepository.findRanksByCursor(
                        periodType,
                        periodValue,
                        parsedCursor.rank(),
                        parsedCursor.dogId(),
                        PageRequest.of(0, pageLimit)
                )
                        : dogRankRepository.findRanksByCursor(
                        periodType,
                        periodValue,
                        regionId,
                        parsedCursor.rank(),
                        parsedCursor.dogId(),
                        PageRequest.of(0, pageLimit)
                ),
                this::toPersonalRankItem,
                item -> rankingCursorCodec.toRankDogCursor(item.getRank(), item.getDogId())
        );

        return PersonalRankingListResponse.of(page.items(), page.nextCursor(), page.hasNext());
    }

    private PersonalRankItemResponse toPersonalRankItem(DogRankView view) {
        return PersonalRankItemResponse.of(
                view.getRank(),
                view.getDogId(),
                view.getDogName(),
                view.getBirthDate(),
                view.getProfileImageUrl(),
                view.getDogBreed(),
                view.getTotalDistance()
        );
    }
}
